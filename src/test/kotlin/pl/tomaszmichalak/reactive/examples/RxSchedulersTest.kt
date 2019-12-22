/*
 * Copyright (C) 2019 Tomasz Michalak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.tomaszmichalak.reactive.examples

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

@ExtendWith(VertxExtension::class)
class RxSchedulersTest {

    private val mainThreadName = "Test worker"

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Expect the current thread.")
    fun testSingle(testContext: VertxTestContext) {
        val threads = Array(2) { mainThreadName }
        Single.just(0)
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .map { it + 1 }
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .subscribe { _ ->
                    testContext.verify {
                        assertEquals(mainThreadName, threads[0])
                        assertEquals(mainThreadName, threads[0])
                        testContext.completeNow()
                    }
                }
        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS))
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Expect the new thread from beginning.")
    fun testSingleWithSubscribeOn(testContext: VertxTestContext) {
        val threads = Array(2) { mainThreadName }
        Single.just(0)
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .map { it + 1 }
                .subscribeOn(Schedulers.io())
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .subscribe { _ ->
                    testContext.verify {
                        assertTrue(threads[0].startsWith("RxCachedThreadScheduler"))
                        assertTrue(threads[1].startsWith("RxCachedThreadScheduler"))
                        testContext.completeNow()
                    }
                }
        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS))
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Expect the new thread after observeOn.")
    fun testSingleWithObserveOn(testContext: VertxTestContext) {
        val threads = Array(2) { mainThreadName }
        Single.just(0)
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .map { it + 1 }
                .observeOn(Schedulers.io())
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .subscribe { _ ->
                    testContext.verify {
                        assertEquals("Test worker", threads[0]);
                        assertTrue(threads[1].startsWith("RxCachedThreadScheduler"))
                        testContext.completeNow()
                    }
                }
        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS))
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Expect the new thread after observeOn even when subscribeOn is before.")
    fun testSingleWithBoth(testContext: VertxTestContext) {
        val threads = Array(3) { mainThreadName }
        Single.just(0)
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .map { it + 1 }
                .subscribeOn(Schedulers.io())
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .map { it + 1 }
                .observeOn(Schedulers.computation())
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .subscribe { _ ->
                    testContext.verify {
                        assertTrue(threads[0].startsWith("RxCachedThreadScheduler"))
                        assertTrue(threads[1].startsWith("RxCachedThreadScheduler"))
                        assertTrue(threads[2].startsWith("RxComputationThreadPool"))
                        testContext.completeNow()
                    }
                }
        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS))
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Expect inner single subscribeOn not affect the previous outer single.")
    fun testInnerSingleWithSubscribeOn(testContext: VertxTestContext) {
        val threads = Array(3) { mainThreadName }

        val inner = Single.just(1)
                .subscribeOn(Schedulers.io())
                .doOnSuccess { threads[it] = Thread.currentThread().name }

        Single.just(0)
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .flatMap { inner }
                .map { it + 1 }
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .subscribe { _ ->
                    testContext.verify {
                        assertEquals("Test worker", threads[0])
                        assertTrue(threads[1].startsWith("RxCachedThreadScheduler"))
                        assertTrue(threads[2].startsWith("RxCachedThreadScheduler"))
                        testContext.completeNow()
                    }
                }
        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS))
    }

    @org.junit.jupiter.api.Test
    @org.junit.jupiter.api.DisplayName("Expect ouher single subscribeOn affect inner one.")
    fun testOuterSingleWithSubscribeOn(testContext: VertxTestContext) {
        val threads = Array(3) { mainThreadName }

        val inner = Single.just(1)
                .doOnSuccess { threads[it] = Thread.currentThread().name }

        Single.just(0)
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .flatMap { inner }
                .subscribeOn(Schedulers.io())
                .map { it + 1 }
                .doOnSuccess { threads[it] = Thread.currentThread().name }
                .subscribe { _ ->
                    testContext.verify {
                        assertTrue(threads[0].startsWith("RxCachedThreadScheduler"))
                        assertTrue(threads[1].startsWith("RxCachedThreadScheduler"))
                        assertTrue(threads[2].startsWith("RxCachedThreadScheduler"))
                        testContext.completeNow()
                    }
                }
        assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS))
    }

}