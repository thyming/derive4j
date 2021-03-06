/*
 * Copyright (c) 2018, Jean-Baptiste Giraudeau <jb@giraudeau.info>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.derive4j.example;

import fj.Equal;
import fj.F;
import fj.F0;
import fj.Hash;
import fj.Ord;
import fj.Show;
import fj.control.Trampoline;
import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Flavour;
import org.derive4j.Instances;

import static org.derive4j.example.Expressions.Add;
import static org.derive4j.example.Expressions.Const;
import static org.derive4j.example.Expressions.Mult;
import static org.derive4j.example.Expressions.expressionHash;
import static org.derive4j.example.Expressions.expressionShow;

@Data(value = @Derive(@Instances({ Show.class, Hash.class, Equal.class, Ord.class })), flavour = Flavour.FJ)
public abstract class Expression {

  public static Integer eval(Expression expression) {

    return stackSafeEval.f(expression).run();
  }

  public static void main(String[] args) {
    Expression expr = Add(Const(1), Mult(Const(2), Mult(Const(3), Const(3))));
    expressionShow().println(expr);
    expressionHash().hash(expr);
    System.out.println(eval(expr)); // (1+(2*(3*3))) = 19
  }

  private static final F<Expression, Integer> eval = Expressions.cata(
      value -> value,
      (left, right) -> left + right,
      (left, right) -> left * right,
      expr -> -expr,
      F0::f);

  private static final F<Expression, Trampoline<Integer>> stackSafeEval = Expressions.cata(
      value -> Trampoline.pure(value),
      (left, right) -> left.zipWith(right, (l, r) -> l + r),
      (left, right) -> left.zipWith(right, (l, r) -> l * r),
      expr -> expr.map(i -> -i),
      Trampoline::suspend);

  public abstract <R> R match(Cases<Expression, R> cases);

  interface Cases<E, R> {
    R Const(int value);

    R Add(E left, E right);

    R Mult(E left, E right);

    R Neg(E expr);
  }
}
