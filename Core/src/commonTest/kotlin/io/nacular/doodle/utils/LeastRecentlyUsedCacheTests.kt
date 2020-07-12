package io.nacular.doodle.utils

import kotlin.reflect.KProperty1
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 6/25/20.
 */
class LeastRecentlyUsedCacheTests {
    @Test @JsName("defaultsWork")
    fun `defaults work`() {
        mapOf(
                LeastRecentlyUsedCache<Any, Any>::size    to 0,
                LeastRecentlyUsedCache<Any, Any>::keys    to emptySet<Any>(),
                LeastRecentlyUsedCache<Any, Any>::values  to emptyList<Any>(),
                LeastRecentlyUsedCache<Any, Any>::entries to emptySet<Any>()
        ).forEach { validateDefault(it.key, it.value) }

        LeastRecentlyUsedCache<Any, Any>(0).apply {
            expect(true) { isEmpty() }
        }
    }

    @Test @JsName("zeroRetainsNothing")
    fun `zero size retains nothing`() {
        val cache = cache<String, String>(0)

        cache["foo"] = "bar"

        expect(0    ) { cache.size     }
        expect(false) { "bar" in cache }
        expect(null ) { cache["foo"]   }
    }

    @Test @JsName("doesNotExceedMaxSize")
    fun `does not exceed max size`() {
        val cache = cache<String, String>(3)

        cache.addAndVerify("a", "A"); expect(1) { cache.size }
        cache.addAndVerify("b", "B"); expect(2) { cache.size }
        cache.addAndVerify("c", "C"); expect(3) { cache.size }
        cache.addAndVerify("d", "D"); expect(3) { cache.size }

        expect(mapOf(
                "b" to "B",
                "c" to "C",
                "d" to "D"
        )) { cache }
    }

    @Test @JsName("overwritesLeastRecentlyUsed")
    fun `overwrites least recently used`() {
        val cache = cache<String, String>(3)

        cache.addAndVerify("a", "A")
        cache.addAndVerify("b", "B")
        cache.addAndVerify("c", "C")

        // touch "a" so "b" is now LRU
        val a = cache["a"]

        cache.addAndVerify("d", "D")

        expect(mapOf(
                "a" to "A",
                "c" to "C",
                "d" to "D"
        )) { cache }
    }

    private fun <K, V> LeastRecentlyUsedCache<K, V>.addAndVerify(key: K, value: V) {
        this[key] = value

        expect(true ) { key in this          }
        expect(value) { this[key]            }
        expect(true ) { containsValue(value) }
    }

    private fun <K, V> cache(size: Int) = LeastRecentlyUsedCache<K, V>(size)

    private fun <K, V, T> validateDefault(p: KProperty1<LeastRecentlyUsedCache<K, V>, T>, default: T?) {
        expect(default, "$p defaults to $default") { p.get(LeastRecentlyUsedCache(0)) }
    }
}