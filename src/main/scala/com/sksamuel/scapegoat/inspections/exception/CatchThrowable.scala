package com.sksamuel.scapegoat.inspections.exception

import com.sksamuel.scapegoat.{ Inspection, InspectionContext, Inspector, Levels }

/** @author Stephen Samuel */
class CatchThrowable extends Inspection("Catch throwable", Levels.Warning) {

  def inspector(context: InspectionContext): Inspector = new Inspector(context) {
    override def postTyperTraverser = Some apply new context.Traverser {

      import context.global._

      def catchesThrowable(cases: List[CaseDef]) = {
        cases.exists {
          // matches t : Throwable
          case CaseDef(Bind(_, Typed(_, tpt)), _, _) if tpt.tpe =:= typeOf[Throwable] => true
          // matches _ : Throwable
          case CaseDef(Typed(_, tpt), _, _) if tpt.tpe =:= typeOf[Throwable] => true
          case _ => false
        }
      }

      override def inspect(tree: Tree): Unit = {
        tree match {
          case Try(_, cases, _) if catchesThrowable(cases) =>
            context.warn(tree.pos, self,
              "Did you intend to catch all throwables? Consider catching a more specific exception class: " +
                tree.toString().take(300))
          case _ => continue(tree)
        }
      }
    }
  }
}

