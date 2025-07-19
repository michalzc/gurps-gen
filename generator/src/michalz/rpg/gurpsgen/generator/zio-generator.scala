package michalz.rpg.gurpsgen.generator

import zio.UIO

import michalz.rpg.gurpsgen.model.TemplateTrait
import michalz.rpg.gurpsgen.model.GeneratedTraits
import zio.Random
import zio.ZIO
import michalz.rpg.gurpsgen.model.TemplateElement
import michalz.rpg.gurpsgen.model.TemplateGroup

extension (templateTrait: TemplateTrait)
  def generateOne: UIO[GeneratedTraits] = ZIO.succeed(GeneratedTraits.fromTemplateTrait(templateTrait))

extension (templateGroup: TemplateGroup)
  def generateOne: UIO[GeneratedTraits] =
    Random.shuffle(templateGroup.elements).flatMap {
      case Nil       => ZIO.succeed(GeneratedTraits.empty)
      case head :: _ => head.generateOne
    }

  def generateWithBudget(budget: Int, numberOfTries: Int): UIO[GeneratedTraits] =
    def loop(results: List[GeneratedTraits] = List.empty, remainedTries: Int = numberOfTries): UIO[GeneratedTraits] = 
      val elemsZ: UIO[List[TemplateElement]] = Random.shuffle(templateGroup.elements)
      // def collect
      ???
    ???

extension (element: TemplateElement)
  def generateWithBudget(budget: Int, numberOfTries: Int = 10): UIO[GeneratedTraits] =
    element match
      case tt: TemplateTrait => tt.generateOne

  def generateOne: UIO[GeneratedTraits] =
    element match
      case tt: TemplateTrait => tt.generateOne
