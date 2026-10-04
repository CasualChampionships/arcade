package net.casual.arcade.model.molang

import net.casual.arcade.model.animation.molang.MolangExpression
import net.casual.arcade.model.animation.molang.MolangScope
import net.casual.arcade.model.animation.molang.exception.MolangCompilationException
import net.casual.arcade.model.animation.molang.exception.MolangEvaluationException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MolangExpressionTests {
    @Test
    fun `numeric strings become constants`() {
        assertTrue(MolangExpression.parse("12.5").isConstant)
        assertTrue(MolangExpression.parse(" -3 ").isConstant)
        assertEquals(-3.0F, MolangExpression.parse(" -3 ").eval(MolangScope()))
        assertEquals(0.0F, MolangExpression.parse("").eval(MolangScope()))
    }

    @Test
    fun `arithmetic and math functions evaluate`() {
        val scope = MolangScope()
        assertEquals(3.0F, MolangExpression.parse("1 + 2").eval(scope))
        assertEquals(5.0F, MolangExpression.parse("math.sqrt(3 * 3 + 4 * 4)").eval(scope))
        assertEquals(1.0F, MolangExpression.parse("math.sin(90)").eval(scope), 0.0001F)
    }

    @Test
    fun `queries read live scope values`() {
        val scope = MolangScope()
        val expression = MolangExpression.parse("q.anim_time * 2 + query.life_time")
        scope.animtime = 1.5F
        scope.lifetime = 10.0F
        assertEquals(13.0F, expression.eval(scope))
        scope.animtime = 3.0F
        assertEquals(16.0F, expression.eval(scope))
    }

    @Test
    fun `unknown queries fail to evaluate`() {
        assertThrows<MolangEvaluationException> { MolangExpression.parse("q.missing").eval(MolangScope()) }
    }

    @Test
    fun `variables persist`() {
        val scope = MolangScope()
        assertEquals(9.0F, MolangExpression.parse("v.written = 9; return v.written;").eval(scope))
        assertEquals(18.0F, MolangExpression.parse("v.written * 2").eval(scope))
    }

    @Test
    fun `temporaries do not persis`() {
        val scope = MolangScope()
        assertEquals(5.0F, MolangExpression.parse("t.x = 5; return t.x;").eval(scope))
        assertThrows<MolangEvaluationException> { MolangExpression.parse("return t.x;").eval(scope) }
    }

    @Test
    fun `constant expressions fold at parse time`() {
        val folded = MolangExpression.parse("1 + 2 * 3")
        assertTrue(folded.isConstant)
        assertEquals(7.0F, folded.eval(MolangScope()))
        assertFalse(MolangExpression.parse("q.anim_time + 1").isConstant)
    }

    @Test
    fun `identical sources share one compiled expression`() {
        assertSame(MolangExpression.parse("q.anim_time * 3"), MolangExpression.parse("q.anim_time * 3"))
        assertSame(MolangExpression.parse("q.anim_time * 3"), MolangExpression.parse("  q.anim_time * 3 "))
    }

    @Test
    fun `negation applies to compiled and constant expressions`() {
        val scope = MolangScope()
        scope.animtime = 2.0F
        assertEquals(-4.0F, MolangExpression.parse("q.anim_time * 2").negate().eval(scope))
        assertEquals(-4.0F, MolangExpression.parse("4").negate().eval(scope))
        assertTrue(MolangExpression.parse("4").negate().isConstant)
    }

    @Test
    fun `invalid expressions fail at parse time`() {
        assertThrows<MolangCompilationException> { MolangExpression.parse("math.sin(") }
        assertThrows<MolangCompilationException> { MolangExpression.parse("1 +") }
    }
}
