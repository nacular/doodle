package io.nacular.doodle.layout.constraints

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.nacular.doodle.layout.constraints.Strength.Companion.Strong
import io.nacular.doodle.layout.constraints.Strength.Companion.Weak
import io.nacular.doodle.layout.constraints.impl.Solver
import io.nacular.doodle.utils.diff.Delete
import io.nacular.doodle.utils.diff.Insert
import io.nacular.doodle.utils.diff.compare
import io.nacular.doodle.utils.ifNull
import org.junit.jupiter.api.Test
import kotlin.Double.Companion.NEGATIVE_INFINITY
import kotlin.math.max
import kotlin.math.min
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 10/20/22.
 */
class SolverTests {
    @Test fun `three properties sum`() {
        val parentWidth  = property("parent.width")
        val aLeft  = property("a.left" )
        val aWidth = property("a.width", 400.0)
        val bLeft  = property("b.left" )
        val bWidth = property("b.width")
        val cLeft  = property("c.left" )
        val cWidth = property("c.width" )

        with(Solver()) {
            sync(constraints {
                parentWidth eq 1200

                listOf(parentWidth, aWidth, bWidth, cWidth).forEach { it greaterEq 0 }

                aLeft   eq     0
                aWidth  hint   aWidth.value strength Weak
                aWidth  lessEq parentWidth / 3
                bLeft   eq     aLeft + aWidth
                cLeft   eq     bLeft + bWidth

                aWidth + bWidth + cWidth eq parentWidth
            })

            updateVariables()

            expect(parentWidth()) { listOf(aWidth, bWidth, cWidth).sumOf { it() } }

            expect(400.0) { aWidth() }
            expect(800.0) { bWidth() }
            expect(  0.0) { cWidth() }

            expect(0.0 ..  400.0) { computeRange(aWidth, 0.0) }
            expect(0.0 .. 1200.0) { computeRange(bWidth, 0.0) }
            expect(0.0 .. 1200.0) { computeRange(cWidth, 0.0) }
        }
    }

    @Test fun `two properties sum`() {
        val parentWidth  = property("parent.width_", 1200.0)
        val parentHeight = property("parent.height_", 1200.0)
        val aLeft  = property("child1.x_" )
        val aWidth = property("child1.width_", 100.0)
        val bLeft  = property("child2.x_" )
        val bWidth = property("child2.width_")

        with(Solver()) {
            sync(constraints {
                parentWidth eq        1200 strength Strong
                parentWidth greaterEq 0

                parentHeight eq        1200 strength Strong
                parentHeight greaterEq 0

                aLeft   eq        0
                aWidth  greaterEq 0
                aWidth  hint      aWidth.value strength Weak
                aWidth  lessEq    parentWidth / 3

                bLeft   eq        aLeft + aWidth
                bWidth  greaterEq 0

                aWidth + bWidth eq parentWidth
            })

            updateVariables()

            expect(parentWidth()) { listOf(aWidth, bWidth).sumOf { it() } }

            expect( 100.0) { aWidth() }
            expect(1100.0) { bWidth() }

            expect(true) { isUnderConstrained(aWidth) }

            expect(  0.0 ..  400.0) { computeRange(aWidth, min = 0.0) }
            expect(800.0 .. 1200.0) { computeRange(bWidth, min = 0.0) }

            aWidth(200.0)
            suggestValue(aWidth, 200.0)

            updateVariables()

            expect(parentWidth()) { listOf(aWidth, bWidth).sumOf { it() } }

            expect( 200.0) { aWidth() }
            expect(1000.0) { bWidth() }

            expect(  0.0 ..  400.0) { computeRange(aWidth, 0.0) }
            expect(800.0 .. 1200.0) { computeRange(bWidth, 0.0) }
        }
    }

    @Test fun `update constraint`() {
        val property = property()

        listOf(
            (constraints { property eq 45                 } to 45) to (constraints { property eq 10                 } to 10),
//            (constraints { property eq 45 strength Weak   } to 45) to (constraints { property eq 10 strength Weak   } to 10),
//            (constraints { property eq 45 strength Strong } to 45) to (constraints { property eq 10 strength Strong } to 10),
        ).forEach { (old, new) ->
            val oldConstraint = old.first.constraints.first()
            val oldValue      = old.second.toDouble()
            val newConstraint = new.first.constraints.first()
            val newValue      = new.second.toDouble()

            Solver().apply {
                addConstraint(oldConstraint)

                updateVariables()

                verify(exactly = 1) { property(oldValue) }

                updateConstant(oldConstraint, newConstraint)

                updateVariables()

                verify(exactly = 1) { property(newValue) }

                clearMocks(property, recordedCalls = true, answers = false)
            }
        }
    }

    @Test fun `under constrained`() {
        val x = property("x")
        val y = property("y")
        val z = property("z")

        mapOf(
            constraints {
                x hint   100 strength Weak
                x lessEq 400
            } to true,
            constraints {
                x(100.0)
                val a = property("a")
                val p = property("p", 1200.0)

                listOf(p, x, a).forEach { it greaterEq 0 }

                y     eq       0
                x     hint     100 strength Weak
                x     lessEq   p / 3
                z     eq       y + x
                x + a eq       p
            } to true,
            constraints { x lessEq    45 } to true,
            constraints {
                x lessEq    45
                x greaterEq 10
                x hint      10
            } to true,
            constraints { x lessEq    z  } to true,
            constraints { x greaterEq 45 } to true,
            constraints {
                x lessEq    100
                x greaterEq 45
            } to true,
            constraints {
                y lessEq 45
                z eq 10
                x eq y - z
            } to true,

            constraints {
                y eq 45 strength Strong
                x eq y
            } to false,
            constraints { x eq 45 } to false,
            constraints {
                y eq 45
                z eq 10
                x eq y - z
            } to false,

            constraints {
                x greaterEq 0

                y eq        61 strength Strong
                y greaterEq 0

                z eq 30
                z eq z

                z + x eq y - 30
                z + x eq z + x
            } to false,
            constraints {
                y eq 1728
                y eq x strength Strong

                z greaterEq 0
                z eq y - x
                x greaterEq 0

                z eq 0.5 * (y - x)
            } to false,
        ).forEach { (context, expected) ->
            Solver().apply {
                sync(context)

                expect(expected, "$x should ${ if (expected) "" else "not " }be under-constrained") { isUnderConstrained(x) }
            }
        }
    }

    @Test fun `unknown is under constrained`() {
        val x = property()

        expect(true, "$x should be under-constrained") { Solver().isUnderConstrained(x) }
    }

    @Test fun `min works`() {
        val x = property()
        val a = property()
        val b = property()

        listOf(
            constraints {
                a eq 10
                b eq 100
            },
            constraints {
                a eq -10
                b eq -100
            },
            constraints {
                a eq 10
                b eq 2 * a
            },
            constraints {
                a eq 10
                b greaterEq a + 2
            },
            constraints {
                a greaterEq 2
                a lessEq    10
                b eq        11
            },
            constraints {
                a eq 2
                b eq a
            },
        ).forEach { context ->
            Solver().apply {
                (context.constraints + constraints { x eq min(a, b) }.constraints).forEach {
                    addConstraint(it)
                }

                updateVariables()

                expect(min(a(), b())) { x.invoke() }
            }
        }
    }

    @Test fun `max works`() {
        val x = property()
        val a = property()
        val b = property()

        listOf(
            constraints {
                a eq 10
                b eq 100
            },
            constraints {
                a eq -10
                b eq -100
            },
            constraints {
                a eq 10
                b eq 2 * a
            },
            constraints {
                a eq 10
                b greaterEq a + 2
            },
            constraints {
                a greaterEq 2
                a lessEq    10
                b eq        11
            },
            constraints {
                a eq 2
                b eq a
            },
        ).forEach { context ->
            Solver().apply {
                (context.constraints + constraints { x eq max(a, b) }.constraints).forEach {
                    addConstraint(it)
                }

                updateVariables()

                expect(max(a(), b())) { x.invoke() }
            }
        }
    }

    @Test fun `min of n works`() {
        val x = property()
        val a = property()
        val b = property()
        val c = property()

        listOf(
            constraints {
                a eq 10
                b eq 100
                c eq 200
            },
            constraints {
                a eq -10
                b eq -100
                c eq -200
            },
            constraints {
                a eq 10
                b eq 2 * a
                c eq 3 * a
            },
            constraints {
                a eq 10
                b greaterEq a + 2
                c lessEq a + 3
            },
            constraints {
                a greaterEq 2
                a lessEq    10
                b eq        11
                c eq 3
            },
            constraints {
                a eq 2
                b eq a
                c eq b
            },
        ).forEach { context ->
            Solver().apply {
                (context.constraints + constraints { x eq min(a, b, c) }.constraints).forEach {
                    addConstraint(it)
                }

                updateVariables()

                expect(minOf(a(), b(), c())) { x.invoke() }
            }
        }
    }

    @Test fun `max of n works`() {
        val x = property()
        val a = property()
        val b = property()
        val c = property()

        listOf(
            constraints {
                a eq 10
                b eq 100
                c eq 200
            },
            constraints {
                a eq -10
                b eq -100
                c eq -200
            },
            constraints {
                a eq 10
                b eq 2 * a
                c eq 3 * a
            },
            constraints {
                a eq 10
                b greaterEq a + 2
                c lessEq a + 3
            },
            constraints {
                a greaterEq 2
                a lessEq    10
                b eq        11
                c eq 3
            },
            constraints {
                a eq 2
                b eq a
                c eq b
            },
        ).forEach { context ->
            Solver().apply {
                (context.constraints + constraints { x eq max(a, b, c) }.constraints).forEach {
                    addConstraint(it)
                }

                updateVariables()

                expect(maxOf(a(), b(), c())) { x.invoke() }
            }
        }
    }

    @Test fun `assign to readonly`() {
        val a = property("a")
        val b = property("b")
        val c = property("c")

        val constraints = constraints {
            a eq 10
            b eq 12
            c eq readonly(a) + b
        }

        Solver().apply {
            sync(constraints)

            updateVariables()

            expect(22.0) { c() }
        }
    }

    @Test fun `readonly works in this case`() {
        val a = property("a", 10.0)
        val b = property("b")

        listOf(
            constraints {
                b eq readonly(a) + 10
            },
        ).forEach { constraints ->
            Solver().apply {
                sync(constraints)

                updateVariables()

                expect(10.0) { a() }
                expect(20.0) { b() }
            }
        }
    }

    @Test fun `readonly immutable property`() {
        val a = property("a")
        val b = property("b")
        val c = property("c")

        listOf(
            constraints {
                a eq 10 strength Weak
                b eq 12 strength Weak
                c eq readonly(a) + b
                c eq 24
            },
            constraints {
                a eq 10 strength Weak
                b eq 12 strength Weak
                c eq readonly(readonly(a)) + b
                c eq 24
            },
        ).forEach { constraints ->
            Solver().apply {
                sync(constraints)

                updateVariables()

                expect(10.0) { a() } // a stays fixed
                expect(14.0) { b() } // b is adjusted to satisfy constraints
                expect(24.0) { c() }
            }
        }
    }

    @Test fun `readonly immutable expression`() {
        val a = property("a")
        val b = property("b")
        val c = property("c")
        val d = property("d")

        listOf(
            constraints {
                a eq 10 strength Weak
                b eq 12 strength Weak
                c eq 14 strength Weak
                d eq readonly(a + b) + c
                d eq 44
            },
            constraints {
                a eq 10 strength Weak
                b eq 12 strength Weak
                c eq 14 strength Weak
                d eq readonly(a) + readonly(b) + c
                d eq 44
            },
            constraints {
                a eq 10 strength Weak
                b eq 12 strength Weak
                c eq 14 strength Weak
                d eq readonly(readonly(a) + readonly(b)) + c
                d eq 44
            },
        ).forEach { constraints ->
            Solver().apply {
                sync(constraints)

                updateVariables()

                expect(10.0) { a() } // a stays fixed
                expect(12.0) { b() } // b is adjusted to satisfy constraints
                expect(22.0) { c() } // c is adjusted to satisfy constraints
                expect(44.0) { d() }
            }
        }
    }

    @Test fun `range works`() {
        val a        = property("a")
        val b        = property("b")
        val c        = property("c")
        val d        = property("d", 100.0)
        val e        = property("e")
        val constant = 1200.0

        listOf(
            constraints {
                b     eq 1200
            } to 1200.0 .. 1200.0,
            constraints {
                b     lessEq 1005
            } to NEGATIVE_INFINITY .. 1005.0,
            constraints {
                a     eq        0
                b     greaterEq 0
                b     lessEq    constant / 3
                c     eq        a + b
                b + d eq        constant
                d     greaterEq 0
            } to 0.0 .. constant / 3,
            constraints {
                a     eq        0
                b     greaterEq 0
                b     lessEq    constant / 3
                b     greaterEq 10
                c     eq        a + b
                b + d eq        constant
            } to 10.0 .. constant / 3,
            constraints {
                a     eq        0
                b     greaterEq 0
                b     lessEq    e / 3
                c     eq        a + b
                d     greaterEq 0
                b + d eq        e
                e     eq        constant //strength Strong
                e     greaterEq 0
            } to 0.0 .. constant / 3,
        ).forEach { (constraints, range) ->
            Solver().apply {
                sync(constraints)

                updateVariables()

                expect(range) { computeRange(b) }
            }
        }
    }
}

class ConstraintTests {
    @Test fun `differs by constant`() {
        val property = property()

        val constraint1 = constraints {
            property eq 45
        }.constraints.first()

        val constraint2 = constraints {
            property eq -45
        }.constraints.first()

        expect(true) { constraint1.differsByConstantOnly(constraint2) }
        expect(true) { constraint2.differsByConstantOnly(constraint1) }
    }

    @Test fun diffs() {
        val property1 = property()
        val property2 = property()
        val property3 = property()
        val property4 = property()
        val property5 = property()

        val old = constraints {
            property1             eq  45
            property2             eq 100
            property3 + property4 eq  50
            property5             eq  90
        }

        val new = constraints {
            property1             eq - 45
//            property2             eq -100
            property3 + property4 eq - 50
//            property5             eq   90

//            property1             eq   45
//            property2             eq -100
//            property3 + property4 eq - 50
//            property5             eq   90
        }

        val diffs = compare(old.constraints, new.constraints) { a, b -> a.hashCode() == b.hashCode() }

        var previousDelete: Delete<Constraint>? = null

        val handlePreviousDelete: () -> Unit = {
            previousDelete = null
        }

        diffs.forEach { diff ->
            when (diff) {
                is Delete -> {
                    handlePreviousDelete()
                    previousDelete = diff
                }
                is Insert -> {
                    diff.items.forEachIndexed { index, insertedConstraint ->
                        // address previous delete
                        previousDelete?.let {
                            val deletedConstraint = it.items[index]

                            if (index < it.items.size && insertedConstraint.differsByConstantOnly(deletedConstraint)) {
                                // update constraint
                                println("update: $deletedConstraint -> $insertedConstraint")
                            } else {
                                // handle delete
                                println("delete: $deletedConstraint")
                                // handle insert
                                println("insert: $insertedConstraint")
                            }
                        }.ifNull {
                            // handle insert
                            println("handle insert: $insertedConstraint")
                        }
                    }

                    previousDelete = null
                }
                else -> {
                    handlePreviousDelete()
                }
            }
        }

//        println(diffs)
    }
}

private fun Solver.sync(context: ConstraintDslContext) {
    context.constraints.forEach { addConstraint(it) }
    context.edits.forEach {
        addEditVariable(it.property, it.strength)
        suggestValue   (it.property, it.value   )
    }
}

private abstract class MockVariable: Property(), Variable

private fun property(initial: Double = 0.0): MockVariable = property(null, initial)

private fun property(name: String?, initial: Double = 0.0): MockVariable = mockk<MockVariable> {
    val value = slot<Double>()

    every { this@mockk.value } answers { this@mockk.invoke() }
    every { this@mockk.invoke() } answers { if (value.isCaptured) value.captured else initial }
    every { this@mockk.invoke(capture(value)) } returns Unit
    every { this@mockk.toString() } answers { name ?: callOriginal() }

    every { toTerm() } returns VariableTerm(this)
    every { this@mockk.terms } returns arrayOf(VariableTerm(this))
    every { this@mockk.constant } returns 0.0 //invoke()
    every { this@mockk.variable } returns this
}

private fun constraints(block: ConstraintDslContext.() -> Unit): ConstraintDslContext {
    val context = ConstraintDslContext()

    context.apply(block)

    return context
}