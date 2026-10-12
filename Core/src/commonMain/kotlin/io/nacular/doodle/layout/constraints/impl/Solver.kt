package io.nacular.doodle.layout.constraints.impl

import io.nacular.doodle.layout.constraints.Constraint
import io.nacular.doodle.layout.constraints.ConstraintException
import io.nacular.doodle.layout.constraints.DuplicateConstraintException
import io.nacular.doodle.layout.constraints.Expression
import io.nacular.doodle.layout.constraints.IdealEdit
import io.nacular.doodle.layout.constraints.Operator.EQ
import io.nacular.doodle.layout.constraints.Operator.GE
import io.nacular.doodle.layout.constraints.Operator.LE
import io.nacular.doodle.layout.constraints.ReferenceVariable
import io.nacular.doodle.layout.constraints.Strength
import io.nacular.doodle.layout.constraints.Strength.Companion.Required
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.UnsatisfiableConstraintException
import io.nacular.doodle.layout.constraints.Variable
import io.nacular.doodle.layout.constraints.VariableTerm
import io.nacular.doodle.layout.constraints.impl.Solver.Type.Dummy
import io.nacular.doodle.layout.constraints.impl.Solver.Type.Error
import io.nacular.doodle.layout.constraints.impl.Solver.Type.External
import io.nacular.doodle.layout.constraints.impl.Solver.Type.Invalid
import io.nacular.doodle.layout.constraints.impl.Solver.Type.Slack
import io.nacular.doodle.utils.fastMutableMapOf
import io.nacular.doodle.utils.fastMutableSetOf
import kotlin.Double.Companion.MAX_VALUE
import kotlin.Double.Companion.NEGATIVE_INFINITY
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * Classes are derived works from [https://github.com/alexbirkett/kiwi-java/tree/master/src/main/java/no/birkett/kiwi],
 * which have the following copyright.
 *
 * Copyright (c) 2015, Alex Birkett
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 *  * Neither the name of kiwi-java nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Unrecoverable error within the Solver.
 */
internal class InternalSolverError(message: String): ConstraintException(message)

internal class DuplicateEditVariableException: Exception()

internal class RequiredFailureException: Exception()

internal class Solver {
    private class Tag {
        var marker            = Symbol()
        var other             = Symbol()
        var markerCoefficient = 1.0 // dummy default
    }

    private class EditInfo(var constraint: Constraint, var tag: Tag, var constant: Double)

    private enum class Type {
        Invalid, External, Slack, Error, Dummy
    }

    private class Symbol(val type: Type = Invalid) {
        private val id = nextId++

        override fun hashCode() = id
        override fun equals  (other: Any?) = this === other
        override fun toString() = "$type"

        private companion object { var nextId = 0 }
    }

    private inner class Row {
        var symbol: Symbol? = null; set(new) {
            field = new

            if (new == null) {
                cells.keys.forEach {
                    unregisterSymbol(it)
                }
            }
        }

        var cells   = fastMutableMapOf<Symbol, Double>(); private set
        var constant: Double

        constructor(constant: Double = 0.0) {
            this.constant = constant
        }

        constructor(other: Row) {
            cells.putAll(other.cells)
            constant = other.constant

            cells.keys.forEach {
                registerSymbol(it)
            }
        }

        private fun registerSymbol  (symbol: Symbol) { rowsWithSymbol.getOrPut(symbol) { fastMutableSetOf() }.add(this) }
        private fun unregisterSymbol(symbol: Symbol) {
            rowsWithSymbol[symbol]?.let {
                it.remove(this)
                if (it.isEmpty()) {
                    rowsWithSymbol.remove(symbol)
                }
            }
        }

        /**
         * Add a constant value to the row constant.
         *
         * @return The new value of the constant
         */
        fun add(value: Double) = value.let { constant += it; constant }

        /**
         * Insert a symbol into the row with a given coefficient.
         *
         * If the symbol already exists in the row, the coefficient will be
         * added to the existing coefficient. If the resulting coefficient
         * is zero, the symbol will be removed from the row
         */
        fun insert(symbol: Symbol, coefficient: Double = 1.0) {
            val newCoefficient = cells.getOrPut(symbol) {
                registerSymbol(symbol)
                0.0
            } + coefficient

            when {
                nearZero(newCoefficient) -> cells.remove(symbol).also { unregisterSymbol(symbol) }
                else                     -> cells[symbol] = newCoefficient
            }
        }

        /**
         * Insert a row into this row with a given coefficient.
         * The constant and the cells of the other row will be multiplied by
         * the coefficient and added to this row. Any cell with a resulting
         * coefficient of zero will be removed from the row.
         *
         * @param other
         */
        fun insert(other: Row, coefficient: Double = 1.0) {
            constant += other.constant * coefficient

            for ((symbol, value) in other.cells) {
                insert(symbol, value * coefficient)
            }
        }

        /**
         * Reverse the sign of the constant and all cells in the row.
         */
        fun reverseSign() {
            constant = -constant
            for ((symbol,value) in cells) {
                cells[symbol] = -value
            }
        }

        /**
         * Solve the row for the given symbol.
         *
         * This method assumes the row is of the form a * x + b * y + c = 0
         * and (assuming solve for x) will modify the row to represent the
         * right-hand side of x = -b/a * y - c / a. The target symbol will
         * be removed from the row, and the constant and other cells will
         * be multiplied by the negative inverse of the target coefficient.
         * The given symbol *must* exist in the row.
         *
         * @param symbol
         */
        fun solveFor(symbol: Symbol) {
            val coefficient = -1.0 / cells[symbol]!!
            cells.remove(symbol).also { unregisterSymbol(symbol) }
            constant *= coefficient
            for ((key, value) in cells) {
                cells[key] = value * coefficient
            }
        }

        /**
         * Solve the row for the given symbols.
         *
         * This method assumes the row is of the form x = b * y + c and will
         * solve the row such that y = x / b - c / b. The rhs symbol will be
         * removed from the row, the lhs added, and the result divided by the
         * negative inverse of the rhs coefficient.
         * The lhs symbol *must not* exist in the row, and the rhs symbol
         * must* exist in the row.
         *
         * @param lhs
         * @param rhs
         */
        fun solveFor(lhs: Symbol, rhs: Symbol) {
            insert(lhs, -1.0)
            solveFor(rhs)
        }

        /**
         * Get the coefficient for the given symbol.
         *
         * If the symbol does not exist in the row, zero will be returned.
         *
         * @return
         */
        fun coefficientFor(symbol: Symbol) = cells[symbol] ?: 0.0

        /**
         * Substitute a symbol with the data from another row.
         *
         * Given a row of the form a * x + b and a substitution of the
         * form x = 3 * y + c the row will be updated to reflect the
         * expression 3 * a * y + a * c + b.
         * If the symbol does not exist in the row, this is a no-op.
         */
        fun substitute(symbol: Symbol, row: Row) {
            cells.remove(symbol)?.let { coefficient ->
                unregisterSymbol(symbol)
                insert(row, coefficient)
            }
        }
    }

    private val maxPasses               = 3
    private val constraints             = fastMutableMapOf<Constraint, Tag>()
    private val rows                    = fastMutableMapOf<Symbol, Row>()
    private val rowList                 = mutableListOf<Row>()
    private val vars                    = fastMutableMapOf<Variable, Symbol>()
    private val edits                   = fastMutableMapOf<Variable, EditInfo>()
    private val idealEdits              = fastMutableSetOf<IdealEdit>()
    private val infeasibleRows          = mutableListOf<Symbol>()
    private val referenceVars           = fastMutableSetOf<Variable>()
    private val referenceConstraintDeps = fastMutableMapOf<Variable, MutableSet<Constraint>>()
    private val referentLastValues      = fastMutableMapOf<Variable, Double>()
    private val referenceSymbols        = fastMutableSetOf<Symbol>()
    private val objective               = Row()
    private var artificial              = null as Row?
    private val rowsWithSymbol          = fastMutableMapOf<Symbol, MutableSet<Row>>()
    private val varCounts               = fastMutableMapOf<Variable, Int>()
    private val guards                  = fastMutableMapOf<Variable, Constraint>()

    val variables: Set<Variable> get() = vars.keys

    fun addNegativeGuard(variable: Variable) {
        val constraint = Constraint(Expression(VariableTerm(variable, 1.0)), GE, Required)
        guards[variable] = constraint

        addConstraint(constraint, incrementVariableCounts = false)
    }

    fun cleanupNegativeGuard(variable: Variable) {
        if ((varCounts[variable] ?: 0) <= 0) {
            guards[variable]?.let {
                removeConstraint(it)
            }
        }
    }

    /**
     * Add a constraint to the solver.
     *
     * @param constraint
     * @throws DuplicateConstraintException The given constraint has already been added to the solver.
     * @throws UnsatisfiableConstraintException The given constraint is required and cannot be satisfied.
     */
    fun addConstraint(constraint: Constraint) {
        addConstraint(constraint, incrementVariableCounts = true)
    }

    fun removeConstraint(constraint: Constraint) {
        val tag = constraints.remove(constraint) ?: return

        removeConstraint(constraint, tag)
    }

    fun addEditVariable(variable: Variable, strength: Strength) {
        if (edits.containsKey(variable)) {
            throw DuplicateEditVariableException()
        }

        if (strength == Required) {
            throw RequiredFailureException()
        }

        val constraint = Constraint(Expression(VariableTerm(variable)), EQ, strength)

        addConstraint(constraint)

        edits[variable] = EditInfo(constraint, constraints[constraint]!!, 0.0)
    }

    fun removeEditVariable(variable: Variable) {
        val edit = edits[variable] ?: return

        removeConstraint(edit.constraint)

        edits.remove(variable)
    }

    fun addIdealEdit(edit: IdealEdit) {
        idealEdits += edit
    }

    fun removeIdealEdit(edit: IdealEdit) {
        idealEdits -= edit

        removeEditVariable(edit.property)
    }

    fun suggestValue(variable: Variable, value: Double) {
        if (shiftEdit(variable, value)) dualOptimize()
    }

    fun updateConstant(old: Constraint, new: Constraint) {
        val tag   = constraints.remove(old) ?: return
        val delta = (new.expression.constant - old.expression.constant) * tag.markerCoefficient
        constraints[new] = tag

        if (delta != 0.0) {
            updateRow(rows[tag.marker], tag, delta)
        }
    }

    /**
     * Update the values of the external solver variables.
     */
    fun updateVariables() {
        finalizeVariables(skipReadOnly = true)

        repeat(maxPasses) { // keep the last result if it doesn't converge
            var changed = false
            if (updateReadOnlyVariables()) { finalizeVariables(); changed = true }
            if (applyIdealEdits        ()) { finalizeVariables(); changed = true }
            if (!changed                 ) return // stable: nothing moved this pass
        }
    }

    private fun applyIdealEdits(): Boolean {
        var changed = false

        // structure first: adding an edit variable is a structural change and settles the tableau
        for (edit in idealEdits) {
            if (edit.property !in edits) { addEditVariable(edit.property, edit.strength); changed = true }
        }

        // then values: shift all rows, repair once
        for (edit in idealEdits) {
            if (shiftEdit(edit.property, edit.invoke())) changed = true
        }

        if (changed) dualOptimize()

        return changed
    }

    private fun shiftEdit(variable: Variable, value: Double): Boolean {
        val info  = edits[variable] ?: return false
        val delta = value - info.constant

        if (nearZero(delta)) return false // tolerance, so float noise can't keep it dirty

        info.constant = value
        shiftRow(rows[info.tag.marker], info.tag, delta)

        return true
    }

    fun finalizeVariables(skipReadOnly: Boolean = false) {
        vars.forEach { (variable, symbol) ->
            if (skipReadOnly && symbol in referenceSymbols) return@forEach

            rows[symbol]?.let { row ->
                variable(row.constant.cleanUp())
            }
        }
    }

    fun updateReadOnlyVariables(): Boolean {
        var furtherResolutionRequired = false

        referenceVars.forEach { variable ->
            val lastValue = referentLastValues[variable] ?: return@forEach
            val current   = variable()
            val delta     = current - lastValue

            if (delta != 0.0) {
                furtherResolutionRequired = true
                referentLastValues[variable] = current
                referenceConstraintDeps[variable]?.forEach { constraint ->
                    val tag = constraints[constraint] ?: return@forEach
                    shiftRow(rows[tag.marker], tag, delta)
                }
            }
        }

        if (furtherResolutionRequired) dualOptimize()   // one repair, after all deltas are in

        return furtherResolutionRequired
    }

    fun isUnderConstrained(v: Variable): Boolean = vars[v]?.let { isUnderConstrained(it) } ?: true // unknown variable = free

    private fun isUnderConstrained(symbol: Symbol) = rows[symbol]?.run {
        cells.any { (sym, coefficient) ->
            if (coefficient == 0.0) return@any false
            when (sym.type) {
                Slack    -> sym !in rows
                External -> sym !in rows
                else     -> false
            }
        } || run {
            cells.forEach { (sym, coefficient) ->
                if (coefficient == 0.0 || sym.type == Dummy) return@run false

                edits.values.forEach { editInfo ->
                    if (sym != editInfo.tag.marker && sym != editInfo.tag.other)  return@run false
                }
            }

            true
        }
    } ?: true

    fun computeRange(variable: Variable, min: Double = -MAX_VALUE, max: Double = MAX_VALUE): ClosedFloatingPointRange<Double> {
        val nominal = variable()
        val symbol  = vars[variable] ?: return nominal..nominal
        val row     = rows[symbol  ] ?: return nominal..nominal

        var maxIncrease = max - nominal
        var maxDecrease = nominal - min
        var foundBound  = false

        // For each non-basic symbol in variable's row, do a ratio test across ALL basic rows that contain that symbol
        for ((symbol, variableRowCoefficient) in row.cells) {
            if (variableRowCoefficient == 0.0) continue
            if (symbol.type == Dummy) continue  // Dummy symbols are Required equality artifacts, never free

            foundBound = true

            val symRow = rows[symbol]

            // If symbol is a basic slack, its row constant directly gives the bound
            if (symbol.type == Slack && symRow == null) {
                // non-basic slack at 0, can only increase → bounds v in one direction
                if (variableRowCoefficient < 0) {
                    maxIncrease = min(maxIncrease, 0.0)  // already at upper bound
                } else {
                    maxDecrease = min(maxDecrease, 0.0)  // already at lower bound
                }
            }

            // For each basic row that contains this symbol
            for ((basicSymbol, basicRow) in rows) {
                val basicRowCoefficient = basicRow.cells[symbol] ?: continue

                if (basicRowCoefficient == 0.0 || basicSymbol == symbol) continue  // skip self-referential row

                // Skip rows belonging to edit variable error symbols
                if (basicSymbol.type == Error && edits.values.any { it.tag.marker == basicSymbol || it.tag.other == basicSymbol }) continue

                // If this row's basic variable is an external free variable, it can absorb the movement — skip it
                if (basicSymbol.type == External && isUnderConstrained(basicSymbol)) continue

                // Simpler: the ratio test directly on basicRow.constant / combined coeff:
                val effectiveCoefficient = basicRowCoefficient / variableRowCoefficient
                val limit                = basicRow.constant   / effectiveCoefficient

                if (effectiveCoefficient > 0) {
                    maxDecrease = min(maxDecrease, limit)
                } else {
                    maxIncrease = min(maxIncrease, -limit)
                }
            }
        }

        val lower = (nominal - maxDecrease).cleanUp()
        val upper = max(lower, (nominal + maxIncrease).cleanUp())

        return when {
            !foundBound -> nominal..nominal
            lower == NEGATIVE_INFINITY || upper == POSITIVE_INFINITY -> perturbationRange(variable, min, max)
            !isUnderConstrained(variable) -> lower..upper
            else -> perturbationRange(variable, min, max)
        }
    }

    private fun perturbationRange(variable: Variable, min: Double, max: Double): ClosedFloatingPointRange<Double> {
        val nominal  = variable()
        val sentinel = 1e10

        fun probe(target: Double): Double {
            var addedEdit = false

            return try {
                if (variable !in edits.keys) {
                    addEditVariable(variable, Strong)
                    addedEdit = true
                }

                suggestValue(variable, target)
                finalizeVariables()

                val result = variable()

                suggestValue(variable, nominal)
                finalizeVariables()

                if (addedEdit) {
                    try { removeEditVariable(variable) } catch (_: Exception) {}
                }

                result
            } catch (_: InternalSolverError) {
                // Solver couldn't reach target — restore and return nominal
                if (addedEdit) {
                    try { removeEditVariable(variable) } catch (_: Exception) {}
                }
                try {
                    addEditVariable(variable, Strong)
                    suggestValue(variable, nominal)
                    finalizeVariables()
                    removeEditVariable(variable)
                } catch (_: Exception) {}

                nominal
            }
        }

        val lower = probe(max(min, -sentinel)).let { if (it <= -sentinel + 1) NEGATIVE_INFINITY else it.cleanUp() }
        val upper = probe(min(max,  sentinel)).let { if (it >=  sentinel - 1) POSITIVE_INFINITY else it.cleanUp() }

        return lower..max(lower, upper)
    }

    private fun addConstraint(constraint: Constraint, incrementVariableCounts: Boolean) {
        if (constraints.isNotEmpty() && constraints.containsKey(constraint)) {
            throw DuplicateConstraintException(constraint)
        }

        val tag = Tag()

        try {
            val row     = createRow(constraint, tag)
            var subject = chooseSubject(row, tag)


            if (subject.type == Invalid && row.cells.keys.all { it.type == Dummy }) {
                subject = when {
                    !nearZero(row.constant) -> throw UnsatisfiableConstraintException(constraint, constraints.keys)
                    else -> tag.marker
                }
            }

            when (subject.type) {
                Invalid -> if (!addWithArtificialVariable(row)) {
                    throw UnsatisfiableConstraintException(constraint, constraints.keys)
                }

                else -> {
                    registerRow(subject, row)
                    row.solveFor(subject)
                    substitute(subject, row)
                }
            }

            constraints[constraint] = tag

            optimize(objective)
        } catch (e: ConstraintException) {
            removeConstraint(constraint, tag)
            throw e
        }

        if (incrementVariableCounts) {
            constraint.expression.terms.forEach { term ->
                varCounts[term.variable] = varCounts.getOrPut(term.variable) { 0 } + 1
            }
        }
    }

    private fun removeConstraint(constraint: Constraint, tag: Tag) {
        removeConstraintEffects(constraint, tag)

        var row = rows[tag.marker]

        when {
            row != null -> unregisterRow(tag.marker, cleanup = true)
            else        -> {
                row         = getMarkerLeavingRow(tag.marker)            ?: throw InternalSolverError("internal solver error")
                val leaving = rows.entries.find { it.value == row }?.key ?: throw InternalSolverError("internal solver error")

                unregisterRow(leaving, cleanup = true)
                row.solveFor(leaving, tag.marker)
                substitute(tag.marker, row)
            }
        }

        constraint.expression.reduce().terms.forEach {
            varCounts.remove(it.variable)?.let { count ->
                when {
                    count <= 1 -> {
                        it.variable.also { variable ->
                            vars.remove(variable).also { referenceSymbols.remove(it) }

                            referenceVars           -= variable
                            referentLastValues      -= variable
                            referenceConstraintDeps -= variable
                        }
                    }
                    else       -> varCounts[it.variable] = count - 1
                }
            }
        }

        optimize(objective)
    }

    // same logic as updateRow, minus the dualOptimize() calls
    private fun shiftRow(row: Row?, tag: Tag, delta: Double) {
        if (row != null) {
            if (row.add(-delta) < 0.0) infeasibleRows.add(tag.marker)
            return
        }
        val other = rows[tag.other]
        if (other != null) {
            if (other.add(delta) < 0.0) infeasibleRows.add(tag.other)
            return
        }
        rowsWithSymbol[tag.marker]?.iterator()?.let {
            while (it.hasNext()) {
                val currentRow = it.next()
                when {
                    currentRow.symbol != null -> {
                        val coefficient = currentRow.coefficientFor(tag.marker)
                        if (coefficient != 0.0 && currentRow.add(delta * coefficient) < 0.0 && currentRow.symbol!!.type != External) {
                            infeasibleRows.add(currentRow.symbol!!)
                        }
                    }
                    else -> it.remove()
                }
            }
        }
    }

    private fun updateRow(row: Row?, tag: Tag, delta: Double) {
        shiftRow(row, tag, delta)
        dualOptimize()
    }

    private fun removeConstraintEffects(constraint: Constraint, tag: Tag) {
        if (tag.marker.type == Error) removeMarkerEffects(tag.marker, constraint.strength)
        if (tag.other.type  == Error) removeMarkerEffects(tag.other,  constraint.strength)
    }

    private fun removeMarkerEffects(marker: Symbol, strength: Strength) {
        val row = rows[marker]

        when {
            row != null -> objective.insert(row,    -strength.value.toDouble())
            else        -> objective.insert(marker, -strength.value.toDouble())
        }
    }

    private fun getMarkerLeavingRow(marker: Symbol): Row? {
        var ratio1 = MAX_VALUE
        var ratio2 = MAX_VALUE
        var first : Row? = null
        var second: Row? = null
        var third : Row? = null

        for (row in rowList) {
            val coefficient = row.coefficientFor(marker)

            if (coefficient == 0.0) {
                continue
            }

            when {
                row.symbol?.type == External -> third = row
                coefficient < 0.0       -> {
                    val ratio = -row.constant / coefficient
                    if (ratio < ratio1) {
                        ratio1 = ratio
                        first  = row
                    }
                }
                else                    -> {
                    val ratio = row.constant / coefficient
                    if (ratio < ratio2) {
                        ratio2 = ratio
                        second = row
                    }
                }
            }
        }

        return first ?: second ?: third
    }

    /**
     * Create a new Row object for the given constraint.
     *
     * The terms in the constraint will be converted to cells in the row.
     * Any term in the constraint with a coefficient of zero is ignored.
     * This method uses the `getVarSymbol` method to get the symbol for
     * the variables added to the row. If the symbol for a given cell
     * variable is basic, the cell variable will be substituted with the
     * basic row.
     *
     * The necessary slack and error variables will be added to the row.
     * If the constant for the row is negative, the sign for the row
     * will be inverted so the constant becomes positive.
     *
     * The tag will be updated with the marker and error symbols to use
     * for tracking the movement of the constraint in the tableau.
     */
    private fun createRow(constraint: Constraint, tag: Tag): Row {
        val expression = constraint.expression.reduce()
        val row        = Row(expression.constant)

        for (term in expression.terms) {
            if (!nearZero(term.coefficient)) {
                when (val variable = term.variable) {
                    is ReferenceVariable -> {
                        val currentValue = variable.initial()
                        row.add(term.coefficient * currentValue)
                        referentLastValues[variable] = currentValue
                        referenceVars += variable
                        referenceConstraintDeps.getOrPut(variable) { fastMutableSetOf() }.add(constraint)
                    }
                    else -> {
                        val symbol = getVarSymbol(term.variable)

                        when (val otherRow = rows[symbol]) {
                            null -> row.insert(symbol, term.coefficient)
                            else -> row.insert(otherRow, term.coefficient)
                        }
                    }
                }
            }
        }

        when (constraint.operator) {
            LE, GE -> {
                val coefficient = if (constraint.operator === LE) 1.0 else -1.0
                val slack  = Symbol(Slack)
                tag.marker = slack
                tag.markerCoefficient = coefficient
                row.insert(slack, coefficient)

                if (constraint.strength < Required) {
                    val error = Symbol(Error)
                    tag.other = error
                    row.insert(error, -coefficient)
                    objective.insert(error, constraint.strength.value.toDouble())
                }
            }
            else   -> {
                when {
                    constraint.strength < Required -> {
                        val errorPlus  = Symbol(Error)
                        val errorMinus = Symbol(Error)
                        tag.marker = errorPlus
                        tag.other  = errorMinus
                        tag.markerCoefficient = -1.0 // matches row.insert(errorPlus, -1.0)
                        row.insert(errorPlus, -1.0)  // v = errorPlus - errorMinus
                        row.insert(errorMinus, 1.0)  // v - errorPlus + errorMinus = 0
                        objective.insert(errorPlus,  constraint.strength.value.toDouble())
                        objective.insert(errorMinus, constraint.strength.value.toDouble())
                    }
                    else                           -> {
                        val dummy = Symbol(Dummy)
                        tag.marker = dummy
                        row.insert(dummy)
                    }
                }
            }
        }

        // Ensure the row as a positive constant.
        if (row.constant < 0.0) {
            row.reverseSign()
        }

        return row
    }

    private fun registerRow(symbol: Symbol, row: Row) {
        rows[symbol] = row.also { it.symbol = symbol }
        rowList += row
    }

    private fun unregisterRow(symbol: Symbol, cleanup: Boolean) {
        rows.remove(symbol)?.let {
            if (cleanup) {
                it.symbol = null
            }
            rowList -= it
        }
    }

    /**
     * Add the row to the tableau using an artificial variable.
     *
     * This will return false if the constraint cannot be satisfied.
     */
    private fun addWithArtificialVariable(row: Row): Boolean {
        //TODO check this

        // Create and add the artificial variable to the tableau
        val art    = Symbol(Slack)
        registerRow(art, row)
        artificial = Row(row)

        // Optimize the artificial objective. This is successful
        // only if the artificial objective is optimized to zero.
        optimize(artificial!!)

        val success = nearZero(artificial!!.constant)

        artificial = null

        // If the artificial variable is basic, pivot the row so that
        // it becomes basic. If the row is constant, exit early.
        val artificialRow = rows[art]
        if (artificialRow != null) {
            /**this looks wrong!!! */
            //rows.remove(rowptr);
            val deleteQueue = rows.filter { (_,row) -> row == artificialRow }.map { it.key }

            deleteQueue.forEach {
                unregisterRow(it, cleanup = true)
            }

            if (artificialRow.cells.isEmpty()) {
                return success
            }

            val entering = anyPivotableSymbol(artificialRow)

            if (entering.type == Invalid) {
                return false // unsatisfiable (will this ever happen?)
            }

            artificialRow.solveFor(art, entering)
            substitute(entering, artificialRow)
            registerRow(entering, artificialRow)
        }

        // Remove the artificial variable from the tableau.
        for ((_, r) in rows) {
            r.cells.remove(art)
        }

        objective.cells.remove(art)
        return success
    }

    /**
     * Substitute the parametric symbol with the given row.
     *
     * This method will substitute all instances of the parametric symbol
     * in the tableau and the objective function with the given row.
     */
    private fun substitute(symbol: Symbol, row: Row) {
        rowsWithSymbol[symbol]?.filter { it.symbol != null }?.forEach {
            val key = it.symbol!!
            it.substitute(symbol, row)
            if (key.type != External && it.constant < 0.0) {
                infeasibleRows.add(key)
            }
        }

        objective.substitute  (symbol, row)
        artificial?.substitute(symbol, row)
    }

    /**
     * Optimize the system for the given objective function.
     *
     * This method performs iterations of Phase 2 of the simplex method
     * until the objective function reaches a minimum.
     *
     * @throws InternalSolverError The value of the objective function is unbounded.
     */
    private fun optimize(objective: Row) {
        while (true) {
            val entering = getEnteringSymbol(objective)
            if (entering.type == Invalid) {
                return
            }
            val (leaving, row) = getLeavingRow(entering) ?: throw InternalSolverError("The objective is unbounded.")

            unregisterRow(leaving, cleanup = false)
            row.solveFor(leaving, entering)
            substitute(entering, row)
            registerRow(entering, row)
        }
    }

    @Throws(InternalSolverError::class)
    private fun dualOptimize() {
        while (infeasibleRows.isNotEmpty()) {
            val leaving = infeasibleRows.removeLast()
            val row     = rows[leaving]
            if (row != null && !nearZero(row.constant) && row.constant < 0.0) {
                val entering = getDualEnteringSymbol(row)
                if (entering.type == Invalid) {
                    throw InternalSolverError("Dual optimize failed")
                }
                unregisterRow(leaving, cleanup = false)
                row.solveFor(leaving, entering)
                substitute(entering, row)
                registerRow(entering, row)
            }
        }
    }

    private fun getDualEnteringSymbol(row: Row): Symbol {
        var ratio    = MAX_VALUE
        var entering = Symbol()

        for ((symbol, value) in row.cells) {
            if (symbol.type != Dummy) {
                if (value > 0.0) {
                    val newRatio = objective.coefficientFor(symbol) / value
                    if (newRatio < ratio) {
                        ratio    = newRatio
                        entering = symbol
                    }
                }
            }
        }
        return entering
    }

    /**
     * Get the first Slack or Error symbol in the row.
     *
     *
     * If no such symbol is present, and Invalid symbol will be returned.
     */
    private fun anyPivotableSymbol(row: Row) = row.cells.keys.firstOrNull { it.type == Slack || it.type == Error } ?: Symbol()

    /**
     * Compute the row which holds the exit symbol for a pivot.
     *
     * This documentation is copied from the C++ version and is outdated
     *
     * This method will return an iterator to the row in the row map
     * which holds the exit symbol. If no appropriate exit symbol is
     * found, the end() iterator will be returned. This indicates that
     * the objective function is unbounded.
     */
    private fun getLeavingRow(entering: Symbol): Pair<Symbol, Row>? {
        var ratio = MAX_VALUE
        var result: Pair<Symbol, Row>? = null

        for (row in rowList) {
            row.symbol?.let { symbol ->
                if (symbol.type != External) {
                    val temp = row.coefficientFor(entering)
                    if (temp < 0) {
                        val tempRatio = -row.constant / temp
                        if (tempRatio < ratio) {
                            ratio = tempRatio
                            result = symbol to row
                        }
                    }
                }
            }
        }
        return result
    }

    /**
     * Get the symbol for the given variable.
     *
     * If a symbol does not exist for the variable, one will be created.
     */
    private fun getVarSymbol(variable: Variable) = vars.getOrPut(variable) {
        Symbol(External).also {
            if (variable.isReference) referenceSymbols.add(it)
        }
    }

    /**
     * Choose the subject for solving for the row
     *
     * This method will choose the best subject for using as the solve
     * target for the row. An invalid symbol will be returned if there
     * is no valid target.
     * The symbols are chosen according to the following precedence:
     * 1) The first symbol representing an external variable.
     * 2) A negative slack or error tag variable.
     * If a subject cannot be found, an invalid symbol will be returned.
     */
    private fun chooseSubject(row: Row, tag: Tag): Symbol {
        for (key in row.cells.keys) {
            if (key.type == External && key !in referenceSymbols) {
                return key
            }
        }

        if ((tag.marker.type == Slack || tag.marker.type == Error) && row.coefficientFor(tag.marker) < 0.0) return tag.marker
        if ((tag.other.type  == Slack || tag.other.type  == Error) && row.coefficientFor(tag.other ) < 0.0) return tag.other

        return Symbol()
    }

    /**
     * Compute the entering variable for a pivot operation.
     *
     * This method will return first symbol in the objective function which
     * is non-dummy and has a coefficient less than zero. If no symbol meets
     * the criteria, it means the objective function is at a minimum, and an
     * invalid symbol is returned.
     */
    private fun getEnteringSymbol(objective: Row) = objective.cells.filter { (key, value) ->
        key.type != Dummy && value < 0.0
    }.firstNotNullOfOrNull {
        it.key
    } ?: Symbol()

    private fun nearZero(value: Double): Boolean = abs(value) < 1.0e-8

    private fun Double.cleanUp() = if (nearZero(this)) 0.0 else this
}