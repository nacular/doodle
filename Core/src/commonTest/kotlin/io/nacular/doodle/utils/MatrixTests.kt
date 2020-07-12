@file:Suppress("FunctionName")

package io.nacular.doodle.utils

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect


/**
 * Created by Nicholas Eddy on 3/2/18.
 */


private typealias m<T> = MatrixImpl<T>
private typealias s<T> = SquareMatrix<T>

private fun parse(text: String): Matrix<Double> = MatrixImpl(text.lines().map {
    it.trim().split(" ").map { it.toDouble() }
})

class MatrixTests {
    @Test @JsName("identityWorks") fun `identity works`() {
        listOf(
            s(this[
              this[ 1.0            ]]) to true,

            s(this[
              this[ 1.0,  0.0,  0.0],
              this[ 0.0,  1.0,  0.0],
              this[ 0.0,  0.0,  1.0]]) to true,

            s(this[
              this[ 2.0,  0.0,  0.0],
              this[ 0.0,  2.0,  0.0],
              this[ 0.0,  0.0,  1.0]]) to false,

            s(this[
              this[-1.0,  0.0,  0.0],
              this[ 0.0, -1.0,  0.0],
              this[ 0.0,  0.0, -1.0]]) to false,

            s(this[
              this[ 0.0,  0.0,  1.0],
              this[ 0.0,  1.0,  0.0],
              this[ 1.0,  0.0,  0.0]]) to false,

            s(this[
              this[ 1.0, -1.0,  1.0],
              this[ 0.0,  1.0,  0.0],
              this[ 0.0,  0.0,  1.0]]) to false,

            s(this[
              this[ 1.0,  0.0      ],
              this[ 0.0,  1.0      ]]) to true,

            s(this[
              this[ 1.0,  0.0,  0.0],
              this[ 1.0,  0.1,  0.0],
              this[ 1.0,  0.0,  0.0]]) to false,

            s(this[
              this[ 1 ]]) to true,

            s(this[
              this[ 1,  0,  0],
              this[ 0,  1,  0],
              this[ 0,  0,  1]]) to true,

            s(this[
              this[ 2,  0,  0],
              this[ 0,  2,  0],
              this[ 0,  0,  1]]) to false,

            s(this[
              this[-1,  0,  0],
              this[ 0, -1,  0],
              this[ 0,  0, -1]]) to false,

            s(this[
              this[ 0,  0,  1],
              this[ 0,  1,  0],
              this[ 1,  0,  0]]) to false,

            s(this[
              this[ 1, -1,  1],
              this[ 0,  1,  0],
              this[ 0,  0,  1]]) to false,

            s(this[
              this[ 1,  0],
              this[ 0,  1]]) to true,

            s(this[
              this[ 1,  0,   0],
              this[ 1.0,  0.1, 0.0], // FIXME: After https://youtrack.jetbrains.com/issue/KT-37715 fixed
              this[ 1,  0,   0]]) to false
        ).forEach {
            expect(it.second, "\n${it.first} \nis identity") { it.first.isIdentity }
        }
    }

    @Test @JsName("inverseWorks") fun `inverse works`() {
        listOf(
            s(this[
              this[1.0          ]]) to
            s(this[
              this[1.0          ]]),

            s(this[
              this[1.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 0.0, 1.0]]) to
            s(this[
              this[1.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 0.0, 1.0]]),

            s(this[
              this[2.0, 0.0],
              this[0.0, 2.0]]) to
            s(this[
              this[0.5, 0.0],
              this[0.0, 0.5]])
        ).forEach {
            expect(it.second, "\n${it.first} \ninverse") { it.first.inverse }
        }
    }

    @Test @JsName("nonInvertibleReturnsNull") fun `non-invertible returns null`() {
        listOf(
            s(this[
              this[1.0, 1.0, 1.0],
              this[1.0, 1.0, 1.0],
              this[1.0, 1.0, 1.0]]) to null,

            s(this[
              this[1, 1],
              this[1, 1]]) to null
        ).forEach {
            expect(it.second, "\n${it.first} \ninverse") { it.first.inverse }
        }
    }

    @Test @JsName("dimensionsWorks") fun `dimensions works`() {
        listOf(
            m(this[
              this[1.0                ]]) to (1 to 1),

            m(this[
              this[1.0, 3.4, 69.4, 0.0]]) to (4 to 1),

            m(this[
              this[1.0, 0.0, 0.0      ],
              this[0.0, 1.0, 0.0      ],
              this[0.0, 0.0, 1.0      ]]) to (3 to 3),

            m(this[
              this[1.0, 0.0           ],
              this[0.0, 1.0           ]]) to (2 to 2),

            m(this[
              this[1.0, 0.0, 0.0      ],
              this[1.0, 0.1, 0.0      ]]) to (3 to 2)
        ).forEach {
            expect(it.second.first,  "$it numColums == ${it.second.first}" ) { it.first.numColumns }
            expect(it.second.second, "$it numRows == ${it.second.second}"  ) { it.first.numRows    }
        }
    }

    @Test @JsName("getWorks") fun `get works`() {
        setOf(
            m(this[
              this[1.0                ]]) to (0 to 0) to 1.0,

            m(this[
              this[1.0, 3.4, 69.4, 0.0]]) to (0 to 1) to 3.4,

            m(this[
              this[1.0, 0.0, 0.0      ],
              this[0.0, 1.0, 0.0      ],
              this[0.0, 0.0, 1.0      ]]) to (2 to 2) to 1.0,

            m(this[
              this[1.0, 0.0           ],
              this[0.0, 1.0           ]]) to (1 to 1) to 1.0,

            m(this[
              this[1.0, 0.0, 0.0      ],
              this[1.0, 0.1, 0.0      ]]) to (1 to 1) to 0.1
        ).forEach {
            val row      = it.first.second.first
            val col      = it.first.second.second
            val matrix   = it.first.first
            val expected = it.second

            expect(expected, "\n$matrix\n[$row,$col] == $expected") { matrix[row, col] }
        }
    }

    @Test @JsName("jaggedThrows") fun `jagged throws`() {
        setOf(this[
                this[1.0, 0.0, 0.0],
                this[0.0, 1.0     ],
                this[0.0, 0.0, 1.0]],
            this[
                this[1.0          ],
                this[1.0, 0.1     ],
                this[1.0, 0.0, 0.0]]
        ).forEach {
            assertFailsWith(IllegalArgumentException::class) { m(it) }
        }
    }

    @Test @JsName("emptyThrows") fun `empty throws`() {
        assertFailsWith(IllegalArgumentException::class) { m(listOf()) }
    }

    @Test @JsName("equalsWorks") fun `equals works`() {
        expect(false) { m(this[this[1.0, 3.4, 69.4, 0.0]]).equals(59.0) }

        listOf(
            s(this[
              this[0.5, 0.0],
              this[0.0, 0.5]]) to
            s(this[
              this[0.5, -0.0],
              this[-0.0, 0.5]]) to true,

            m(this[this[1.0]]) to
            m(this[this[1  ]]) to true,

            m(this[this[1.0]]).let { it to it to true },

            m(this[this[1.0, 3.4, 69.4, 0.0]]) to
            m(this[this[1.0, 3.4, 69.4, 0.0]]) to true,

            s(this[
              this[1.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 0.0, 1.0]]) to
            s(this[
              this[1.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 0.0, 1.0]]) to true,

            m(this[
              this[1.0, 0.0, 0.0],
              this[0.0, 0.0, 1.0]]) to
            m(this[
              this[1.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 0.0, 1.0]]) to false,

            m(this[
              this[1.0, 0.0],
              this[0.0, 1.0]]) to
            m(this[
              this[1.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0]]) to false
        ).forEach {
            expect(it.second, "\n${it.first.first} == \n\n${it.first.second}") { it.first.first == it.first.second }
        }
    }

    @Test @JsName("matrixMultiplyWorks") fun `matrix multiply works`() {
        listOf(
            m(this[this[ 1.0     ]]) to
            m(this[this[ 1.0, 2.0]]) to
            m(this[this[ 1.0, 2.0]]),

            m(this[
              this[ 1.0, 0.0, 0.0],
              this[ 0.0, 1.0, 0.0],
              this[ 0.0, 0.0, 1.0]]) to
            m(this[
              this[ 5.0, 2.0, 7.0],
              this[ 4.0, 5.0, 9.0],
              this[ 8.0, 4.0, 0.0]]) to
            m(this[
              this[ 5.0, 2.0, 7.0],
              this[ 4.0, 5.0, 9.0],
              this[ 8.0, 4.0, 0.0]]),

            m(this[
              this[ 5.0, 2.0, 7.0],
              this[ 4.0, 5.0, 9.0],
              this[ 8.0, 4.0, 0.0]]) to
            m(this[
              this[ 2.0          ],
              this[ 2.0          ],
              this[ 2.0          ]]) to
            m(this[
              this[28.0          ],
              this[36.0          ],
              this[24.0          ]]),

            m(this[
              this[ 5.0, 2.0, 7.0]]) to
            m(this[
              this[ 2.0          ],
              this[ 2.0          ],
              this[ 2.0          ]]) to
            m(this[this[28.0     ]]),

            m(this[
              this[  2.0,  -4.0,   1.0,  3.0],
              this[ -2.0,  -3.0,  -1.0,  4.0],
              this[  5.0,   0.0,   6.0,  7.0],
              this[  8.0,   9.0,  10.0, 11.0]]) to
            m(this[
              this[  2.0,  12.0,  13.0],
              this[ 14.0,  -2.0,   1.0],
              this[ 15.0,  -1.0,   0.0],
              this[  5.0,   6.0,   8.0]]) to
            m(this[
              this[-22.0,  49.0,  46.0],
              this[-41.0,   7.0,   3.0],
              this[135.0,  96.0, 121.0],
              this[347.0, 134.0, 201.0]]),

            s(this[
              this[ 2.0,  0.0,  0.0],
              this[ 0.0,  1.0,  0.0],
              this[ 0.0,  4.0,  1.0]]) to
            s(this[
              this[ 1.0,  0.0,  0.0],
              this[ 0.0,  1.0,  0.0],
              this[ 0.0,  0.0,  1.0]]) to
            s(this[
              this[ 2.0,  0.0,  0.0],
              this[ 0.0,  1.0,  0.0],
              this[ 0.0,  4.0,  1.0]])

        ).forEach {
            expect(it.second, "\n${it.first.first} * \n\n${it.first.second} == \n\n${it.second}") { it.first.first * it.first.second }
        }

        listOf(
            s(this[
              this[2.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 4.0, 1.0]]) to
            s(this[
              this[1.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 0.0, 1.0]]) to
            s(this[
              this[2.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 4.0, 1.0]]),

            s(this[
              this[1.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 0.0, 1.0]]) to
            s(this[
              this[2.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 4.0, 1.0]]) to
            s(this[
              this[2.0, 0.0, 0.0],
              this[0.0, 1.0, 0.0],
              this[0.0, 4.0, 1.0]]),

            s(this[
             this[1.0, 2.0, 3.0],
             this[4.0, 5.0, 6.0],
             this[7.0, 8.0, 9.0]]) to
            s(this[
             this[9.0, 8.0, 7.0],
             this[6.0, 5.0, 4.0],
             this[3.0, 2.0, 1.0]]) to
            s(this[
             this[ 30.0,  24.0, 18.0],
             this[ 84.0,  69.0, 54.0],
             this[138.0, 114.0, 90.0]])
        ).forEach {
            expect(it.second, "\n${it.first.first} * \n\n${it.first.second} == \n\n${it.second}") { it.first.first * it.first.second }
        }
    }

    @Test @JsName("multiplyUnmatchedThrows") fun `multiply unmatched throws`() {
        listOf(
            m(this[
              this[ 1.0, 0.0, 0.0],
              this[ 0.0, 1.0, 0.0],
              this[ 0.0, 0.0, 1.0]]) to
            m(this[
              this[ 5.0, 2.0, 7.0],
              this[ 8.0, 4.0, 0.0]]),

            m(this[
              this[ 5.0, 2.0, 7.0],
              this[ 4.0, 5.0, 9.0],
              this[ 8.0, 4.0, 0.0]]) to
            m(this[
              this[ 2.0          ],
              this[ 2.0          ]]),

            m(this[
              this[ 5.0, 7.0     ]]) to
            m(this[
              this[ 2.0          ],
              this[ 2.0          ],
              this[ 2.0          ]]),

            m(this[
              this[  2.0,  -4.0,   1.0,  3.0],
              this[ -2.0,  -3.0,  -1.0,  4.0],
              this[  8.0,   9.0,  10.0, 11.0]]) to
            m(this[
              this[  2.0,  12.0,  13.0],
              this[ 15.0,  -1.0,   0.0],
              this[  5.0,   6.0,   8.0]])

        ).forEach {
            assertFailsWith(IllegalArgumentException::class, "\n${it.first} * \n${it.second}") { it.first * it.second }
        }
    }

    operator fun <T: Number> get(vararg values: T      ) = values.toList()
    operator fun <T: Number> get(vararg values: List<T>) = values.toList()
}