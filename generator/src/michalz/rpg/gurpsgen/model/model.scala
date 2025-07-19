package michalz.rpg.gurpsgen.model

import cats.Semigroup
import cats.instances.list.*
import cats.syntax.semigroup.*

case class GeneratedTraits(traits: List[String], totalCost: Int)
object GeneratedTraits:
  def fromTemplateTrait(t: TemplateTrait): GeneratedTraits = GeneratedTraits(List(t.name), t.cost)
  def empty: GeneratedTraits                               = GeneratedTraits(List.empty, 0)
  def make(traitName: String, cost: Int): GeneratedTraits  = GeneratedTraits(List(traitName), cost)
  given Semigroup[GeneratedTraits]                         = (x: GeneratedTraits, y: GeneratedTraits) =>
    GeneratedTraits(traits = x.traits |+| y.traits, totalCost = x.totalCost |+| y.totalCost)

sealed trait TemplateElement

case class TemplateTrait(name: String, cost: Int) extends TemplateElement

case class TemplateGroup(elements: List[TemplateElement]) extends TemplateElement

object TemplateGroup:
  def apply(elems: TemplateElement*): TemplateGroup =
    TemplateGroup(elems.toList)

case class TemplateChoice(points: Int, choices: TemplateElement) extends TemplateElement

case class NPCTemplate(
    elements: Map[String, TemplateElement]
)

object NPCTemplate:
  def appy(
      elems: (String, TemplateElement)*
  ): NPCTemplate = NPCTemplate(elems.toMap)
