package tip.analysis

import tip.ast.{AAssignStmt, AIdentifier, AProgram, AstNode, DepthFirstAstVisitor, _}
import tip.solvers.CubicSolver
import tip.util.Log
import tip.ast.AstNodeData.{AstNodeWithDeclaration, DeclarationData}

import scala.language.implicitConversions

class ControlFlowAnalysis(program: AProgram)(implicit declData: DeclarationData)
    extends DepthFirstAstVisitor[Null]
    with Analysis[Map[AstNode, Set[AFunDeclaration]]] {

  val log = Log.logger[this.type]()

  case class Decl(fun: AFunDeclaration) {
    override def toString = s"${fun.name}:${fun.loc}"
  }

  case class AstVariable(n: AstNode) {
    override def toString = n match {
      case fun: AFunDeclaration => s"${fun.name}:${fun.loc}"
      case _ => n.toString
    }
  }

  private val solver = new CubicSolver[AstVariable, Decl]

  val allFunctions = program.funs.toSet

  // Analysis does not accept pointers.
  NoPointers.assertContainsProgram(program)

  /**
    * @inheritdoc
    */
  def analyze() = {
    visit(program, null)
    val sol = solver.getSolution
    log.info(s"Solution is:\n${sol.map { case (k, v) => s"  [[$k]] = {${v.mkString(",")}}" }.mkString("\n")}")
    sol.map(vardecl => vardecl._1.n -> vardecl._2.map(_.fun))
  }

  /**
    * Generates the constraints for the given sub-AST.
    * @param node the node for which it generates the constraints
    * @param arg unused for this visitor
    */
  override def visit(node: AstNode, arg: Null) {

    /**
      * Get the declaration if the supplied AstNode is an identifier,
      * which might be a variable declaration or a function declaration.
      * It returns the node itself, otherwise.
      */
    def decl(n: AstNode): AstNode = n match {
      case id: AIdentifier => id.declaration
      case _ => n
    }

    implicit def toVar(n: AstNode): AstVariable = AstVariable(n)

    node match {
      case fun: AFunDeclaration => ??? //<--- Complete here
      case AAssignStmt(id: AIdentifier, e, _) => ??? //<--- Complete here
      case ACallFuncExpr(target, args, false, _) =>
        // Simple call, resolving function name directly
        decl(target) match {
          case fun: AFunDeclaration =>
            // Add the constraints concerning parameters
            fun.args.zip(args).foreach {
              case (formalParam, actualParam) =>
                solver.addSubsetConstraint(decl(actualParam), formalParam)
            }
            // Add the constraints concerning the return
            solver.addSubsetConstraint(decl(fun.stmts.ret.value), node)
          case _ => ???
        }
      case ACallFuncExpr(target, args, true, _) =>
        // Indirect call, using function pointer
        ??? //<--- Complete here
      case _ =>
    }
    visitChildren(node, null)
  }
}
