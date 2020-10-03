/*
* Copyright 2012 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*     http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package foo

import org.junit.Assert._
import org.junit.Test
import junit.framework.ComparisonFailure
import com.eed3si9n.expecty.{Expecty, VarargsExpecty}

class ExpectyRenderingSpec {
  val assert = new Expecty() // (printAsts = true)
  val expect = new VarargsExpecty

  def isDotty: Boolean =
    scala.util.Try(Class.forName("dotty.DottyPredef$")).isSuccess

  @Test
  def literals(): Unit = {
    outputs("""assertion failed

"abc".length() == 2
      |        |
      3        false
    """) {
      assert {
        "abc".length() == 2
      }
    }
  }

  @Test
  def object_apply(): Unit = {
    outputs("""assertion failed

List() == List(1, 2)
|      |  |
List() |  List(1, 2)
       false
    """) {
      assert {
        List() == List(1, 2)
      }
    }
  }

  @Test
  def object_apply_2(): Unit = {
    outputs("""assertion failed

List(1, 2) == List()
|          |  |
List(1, 2) |  List()
           false
  """) {
      assert {
        List(1, 2) == List()
      }
    }
  }

  @Test
  def infix_operators(): Unit = {
    val str = "abc"

    outputs("""assertion failed

str + "def" == "other"
|   |       |
abc abcdef  false
    """) {
      assert {
        str + "def" == "other"
      }
    }
  }

  @Test
  def null_value(): Unit = {
    val x = null

    outputs("""assertion failed

x == "null"
| |
| false
null
    """) {
      assert {
        x == "null"
      }
    }
  }

//   @Test
//   def value_with_type_hint(): Unit = {
//     val expect = new Expecty(showTypes = true)
//     val x = "123"

//     outputs("""
// x == 123
// | |
// | false (java.lang.Boolean)
// 123 (java.lang.String)
//     """) {
//       expect {
//         x == 123
//       }
//     }
//   }

  @Test
  def arithmetic_expressions(): Unit = {
    val one = 1

    outputs("""assertion failed

one + 2 == 4
|   |   |
1   3   false
    """) {
      assert {
        one + 2 == 4
      }
    }
  }

  @Test
  def property_read(): Unit = {
    val person = Person()

    outputs("""assertion failed

person.age == 43
|      |   |
|      42  false
Person(Fred,42)
    """) {
      assert {
        person.age == 43
      }
    }
  }

  @Test
  def method_call_zero_args(): Unit = {
    val person = Person()
    outputs("""assertion failed

person.doIt() == "pending"
|      |      |
|      done   false
Person(Fred,42)
    """) {
      assert {
        person.doIt() == "pending"
      }
    }
  }

  @Test
  def method_call_one_arg(): Unit = {
    val person = Person()
    val word = "hey"

    outputs("""assertion failed

person.sayTwice(word) == "hoho"
|      |        |     |
|      heyhey   hey   false
Person(Fred,42)
    """) {
      assert {
        person.sayTwice(word) == "hoho"
      }
    }
  }

  @Test
  def method_call_multiple_args(): Unit = {
    val person = Person()
    val word1 = "hey"
    val word2 = "ho"

    outputs("""assertion failed

person.sayTwo(word1, word2) == "hoho"
|      |      |      |      |
|      heyho  hey    ho     false
Person(Fred,42)
    """) {
      assert {
        person.sayTwo(word1, word2) == "hoho"
      }
    }
  }

  @Test
  def method_call_var_args(): Unit = {
    val person = Person()
    val word1 = "foo"
    val word2 = "bar"
    val word3 = "baz"

    outputs("""assertion failed

person.sayAll(word1, word2, word3) == "hoho"
|      |      |      |      |      |
|      |      foo    bar    baz    false
|      foobarbaz
Person(Fred,42)
    """) {
      assert {
        person.sayAll(word1, word2, word3) == "hoho"
      }
    }
  }

  @Test
  def nested_property_reads_and_method_calls(): Unit = {
    val person = Person()

    outputs("""assertion failed

person.sayTwo(person.sayTwice(person.name), "bar") == "hoho"
|      |      |      |        |      |             |
|      |      |      FredFred |      Fred          false
|      |      Person(Fred,42) Person(Fred,42)
|      FredFredbar
Person(Fred,42)

    """) {
      assert {
        person.sayTwo(person.sayTwice(person.name), "bar") == "hoho"
      }
    }
  }

  @Test
  def constructor_call(): Unit = {
    val brand = "BMW"
    val model = "M5"


    if (isDotty) {
      outputs("""assertion failed

Car(brand, model).brand == "Audi"
|   |      |            |
|   BMW    M5           false
BMW M5
    """) {
        assert {
          Car(brand, model).brand == "Audi"
        }
      }
    } else {
      outputs("""assertion failed

Car(brand, model).brand == "Audi"
|   |      |      |     |
|   BMW    M5     BMW   false
BMW M5
    """) {
        assert {
          Car(brand, model).brand == "Audi"
        }
      }
    }
  }

//   @Test
//   def higher_order_methods(): Unit = {
//     outputs("""
// a.map(_ * 2) == b
// | |  |  |    |  |
// | |  |  |    |  List(2, 4, 7)
// | |  |  |    false
// | |  |  <function1>
// | |  scala.collection.generic.GenTraversableFactory$ReusableCBF@...
// | List(2, 4, 6)
// List(1, 2, 3)

//     """) {
//       val a = List(1, 2, 3)
//       val b = List(2, 4, 7)
//       assert {
//         a.map(_ * 2) == b
//       }
//     }
//   }

  @Test
  def tuple(): Unit = {
    if (isDotty) {
      outputs("""assertion failed

(1, 2)._1 == 3
 |     |  |
 (1,2) 1  false
      """) {
      assert {
        (1, 2)._1 == 3
      }
    }
    } else {
      outputs("""assertion failed

(1, 2)._1 == 3
|      |  |
(1,2)  1  false
      """) {
      assert {
        (1, 2)._1 == 3
      }
    }
    }
  }

// function1 vs lambda
//   @Test
//   def case_class(): Unit = {
//     outputs("""
// Some(1).map(_ + 1) == Some(3)
// |       |     |    |  |
// Some(1) |     |    |  Some(3)
//         |     |    false
//         |     <function1>
//         Some(2)
//       """) {
//       assert {
//         Some(1).map(_ + 1) == Some(3)
//       }
//     }
//   }

//   @Test
//   def class_with_package(): Unit = {
//     outputs("""
// collection.mutable.HashMap(1->"a").get(1) == "b"
//                    |       ||      |      |
//                    |       |(1,a)  |      false
//                    |       |       Some(a)
//                    |       scala.Predef$ArrowAssoc@...
//                    HashMap(1 -> a)
//       """) {
//       assert {
//         collection.mutable.HashMap(1->"a").get(1) == "b"
//       }
//     }
//   }

//   @Test
//   def java_static_method(): Unit = {
//     outputs("""
// java.util.Collections.emptyList() == null
//                       |           |
//                       []          false
//       """) {
//       assert {
//         java.util.Collections.emptyList() == null
//       }
//     }
//   }

//   @Test
//   def implicit_conversion(): Unit = {
//     outputs("""
// "fred".slice(1, 2) == "frog"
// |      |           |
// fred   r           false
//       """) {
//       assert {
//         "fred".slice(1, 2) == "frog"
//       }
//     }
//   }

  @Test
  def option_type(): Unit = {
    outputs(
      """assertion failed

Some(23) == Some(22)
|        |  |
Some(23) |  Some(22)
         false
      """) {
      assert {
        Some(23) == Some(22)
      }
    }
  }

  // doesn't compile, fix pending
//  @Test
//  def varargs_conversion() {
//    outputs(
//      """
//fun1(List(1) :_*) == List(1)
//|                 |
//List(1)           true
//      """)
//    {
//      def fun1(p: Int*) = p
//
//      assert {
//        fun1(List(1) :_*) == List(1)
//      }
//    }
//  }

  @Test
  def message(): Unit = {
    val person = Person()
    if (isDotty) {
      outputs("""assertion failed: something something

person.age == 43
|      |   |
|      42  false
Person(Fred,42)
      """) {
        assert(person.age == 43, "something something")
      }
    } else {
      outputs("""assertion failed: something something

assert(person.age == 43, "something something")
       |      |   |
       |      42  false
       Person(Fred,42)
      """) {
        assert(person.age == 43, "something something")
      }
    }
  }

  @Test
  def literalsVarargs(): Unit = {
    val maybeComma = if(isDotty) "" else ","
    outputs(s"""assertion failed

"def".length() == 2$maybeComma
      |        |
      3        false
    """) {
      expect(
        "abc".length() == 3,
        "def".length() == 2,
        "fgh".length() == 3
      )
    }
  }

  def outputs(rendering: String)(expectation: => Unit): Unit = {
    def normalize(s: String) = augmentString(s.trim()).lines.mkString

    try {
      expectation
      fail("Expectation should have failed but didn't")
    }
    catch  {
      case e: AssertionError => {
        val expected = normalize(rendering)
        val actual = normalize(e.getMessage).replaceAll("@[0-9a-f]*", "@\\.\\.\\.")
        if (actual != expected) {
          throw new ComparisonFailure(s"Expectation output doesn't match: ${e.getMessage}",
            expected, actual)
        }
      }
    }
  }

  case class Person(name: String = "Fred", age: Int = 42) {
    def doIt() = "done"
    def sayTwice(word: String) = word * 2
    def sayTwo(word1: String,  word2: String) = word1 + word2
    def sayAll(words: String*) = words.mkString("")
  }

  case class Car(val brand: String, val model: String) {
    override def toString = brand + " " + model
  }
}

