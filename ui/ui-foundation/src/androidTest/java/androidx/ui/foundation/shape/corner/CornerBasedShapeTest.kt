/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.ui.foundation.shape.corner

import androidx.test.filters.SmallTest
import androidx.ui.geometry.RRect
import androidx.ui.graphics.Outline
import androidx.ui.unit.Density
import androidx.ui.unit.Px
import androidx.ui.unit.PxSize
import androidx.ui.unit.dp
import androidx.ui.unit.px
import androidx.ui.unit.toRect
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@SmallTest
@RunWith(JUnit4::class)
class CornerBasedShapeTest {

    @Test
    fun createOutlineCalledWithCorrectParams() {
        val density = Density(2f, 1f)
        val passedSize = PxSize(100.px, 50.px)
        var assertionExecuted = false
        val assertSizes = { size: PxSize,
                            topLeft: Px,
                            topRight: Px,
                            bottomRight: Px,
                            bottomLeft: Px ->
            assertThat(size).isEqualTo(passedSize)
            assertThat(topLeft).isEqualTo(4.px)
            assertThat(topRight).isEqualTo(3.px)
            assertThat(bottomRight).isEqualTo(6.px)
            assertThat(bottomLeft).isEqualTo(25.px)
            assertionExecuted = true
        }
        val impl = Impl(
            topLeft = CornerSize(4.px),
            topRight = CornerSize(3.px),
            bottomRight = CornerSize(3.dp),
            bottomLeft = CornerSize(50),
            onOutlineRequested = assertSizes
        )

        assertThat(impl.createOutline(passedSize, density))
            .isEqualTo(Outline.Rectangle(passedSize.toRect()))

        assertThat(assertionExecuted).isTrue()
    }

    @Test
    fun cornersSizesAreNotLargerThenMinDimension() {
        val density = Density(2f, 1f)
        val sizeWithLargerWidth = PxSize(6.px, 4.px)
        val sizeWithLargerHeight = PxSize(4.px, 6.px)

        val sizesList = mutableListOf<PxSize>()
        val assertSizes = { size: PxSize,
                            topLeft: Px,
                            topRight: Px,
                            bottomRight: Px,
                            bottomLeft: Px ->
            sizesList.add(size)
            assertThat(topLeft).isEqualTo(2.px)
            assertThat(topRight).isEqualTo(2.px)
            assertThat(bottomRight).isEqualTo(1.px)
            assertThat(bottomLeft).isEqualTo(2.px)
        }

        val impl = Impl(
            topLeft = CornerSize(10.px),
            topRight = CornerSize(6.dp),
            bottomRight = CornerSize(1.px),
            bottomLeft = CornerSize(2.px),
            onOutlineRequested = assertSizes
        )

        impl.createOutline(sizeWithLargerWidth, density)
        impl.createOutline(sizeWithLargerHeight, density)

        assertThat(sizesList).isEqualTo(mutableListOf(sizeWithLargerWidth, sizeWithLargerHeight))
    }

    @Test
    fun theSameImplsWithTheSameCornersAreEquals() {
        @Suppress("ReplaceCallWithBinaryOperator")
        assertThat(
            Impl2(
                topLeft = CornerSize(4.px),
                topRight = CornerSize(3.px),
                bottomRight = CornerSize(3.dp),
                bottomLeft = CornerSize(50)
            ).equals(
                Impl2(
                    topLeft = CornerSize(4.px),
                    topRight = CornerSize(3.px),
                    bottomRight = CornerSize(3.dp),
                    bottomLeft = CornerSize(50)
                )
            )
        ).isTrue()
    }

    @Test
    fun differentImplWithTheSameCornersAreNotEquals() {
        @Suppress("ReplaceCallWithBinaryOperator")
        assertThat(
            Impl(
                topLeft = CornerSize(4.px),
                topRight = CornerSize(3.px),
                bottomRight = CornerSize(3.dp),
                bottomLeft = CornerSize(50)
            ).equals(
                Impl2(
                    topLeft = CornerSize(4.px),
                    topRight = CornerSize(3.px),
                    bottomRight = CornerSize(3.dp),
                    bottomLeft = CornerSize(50)
                )
            )
        ).isFalse()
    }

    @Test
    fun copyingUsesCorrectDefaults() {
        val impl = Impl(
            topLeft = CornerSize(4.px),
            topRight = CornerSize(3.px),
            bottomRight = CornerSize(3.dp),
            bottomLeft = CornerSize(50)
        )
        assertThat(impl)
            .isEqualTo(impl.copy(bottomRight = CornerSize(3.dp)))
    }
}

private class Impl(
    topLeft: CornerSize,
    topRight: CornerSize,
    bottomRight: CornerSize,
    bottomLeft: CornerSize,
    private val onOutlineRequested: ((PxSize, Px, Px, Px, Px) -> Unit)? = null
) : CornerBasedShape(topLeft, topRight, bottomRight, bottomLeft) {

    override fun createOutline(
        size: PxSize,
        topLeft: Px,
        topRight: Px,
        bottomRight: Px,
        bottomLeft: Px
    ): Outline {
        onOutlineRequested?.invoke(size, topLeft, topRight, bottomRight, bottomLeft)
        return Outline.Rectangle(size.toRect())
    }

    override fun copy(
        topLeft: CornerSize,
        topRight: CornerSize,
        bottomRight: CornerSize,
        bottomLeft: CornerSize
    ) = Impl(topLeft, topRight, bottomRight, bottomLeft, onOutlineRequested)
}

private class Impl2(
    topLeft: CornerSize,
    topRight: CornerSize,
    bottomRight: CornerSize,
    bottomLeft: CornerSize
) : CornerBasedShape(topLeft, topRight, bottomRight, bottomLeft) {

    override fun createOutline(
        size: PxSize,
        topLeft: Px,
        topRight: Px,
        bottomRight: Px,
        bottomLeft: Px
    ): Outline {
        return Outline.Rounded(RRect(size.toRect()))
    }

    override fun copy(
        topLeft: CornerSize,
        topRight: CornerSize,
        bottomRight: CornerSize,
        bottomLeft: CornerSize
    ) = Impl2(topLeft, topRight, bottomRight, bottomLeft)
}