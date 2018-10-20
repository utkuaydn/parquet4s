package com.mjakubowski84.parquet4s

import org.scalatest.{FlatSpec, Matchers}

class ParquetRecordDecoderSpec extends FlatSpec with Matchers {

  import ParquetRecordDecoder._

  "HNil decoder" should "be used to decode empty record" in {
    case class Empty()

    ParquetRecordDecoder.decode[Empty](RowParquetRecord()) should be(Empty())
  }

  "Primitive value decoder" should "decode record containing primitive values" in {
    case class Primitives(int: Int, string: String)

    val data = Primitives(1, "text")

    val record = RowParquetRecord("int" -> 1, "string" -> "text")

    ParquetRecordDecoder.decode[Primitives](record) should be(data)
  }

  it should "throw exception if record is missing data for non-optional field" in {
    case class Row(requiredField: Int)

    val record = RowParquetRecord()

    a[ParquetRecordDecoder.DecodingException] should be thrownBy ParquetRecordDecoder.decode[Row](record)
  }

  it should "decode record with optional field that has no value" in {
    case class Row(optionalField: Option[Int])

    val record = RowParquetRecord()

    ParquetRecordDecoder.decode[Row](record) should be(Row(None))
  }


  it should "throw exception if type of input filed does not match expected type" in {
    case class Row(int: Int)

    val record = RowParquetRecord("int" -> "I am string but I should be int")

    // TODO should be DecodingException with nice explanation
    a[scala.MatchError] should be thrownBy ParquetRecordDecoder.decode[Row](record)
  }

  "Product decoder" should "decode record containing nested records" in {
    case class NestedRow(int: Int, string: String)
    case class Row(nested1: NestedRow, nested2: NestedRow)

    val data = Row(NestedRow(1, "bla"), NestedRow(2, "ble"))

    val record = RowParquetRecord(
      "nested1" -> RowParquetRecord("int" -> 1, "string" -> "bla"),
      "nested2" -> RowParquetRecord("int" -> 2, "string" -> "ble")
    )

    ParquetRecordDecoder.decode[Row](record) should be(data)
  }

  it should "throw exception if record is missing data for field" in {
    case class NestedRow(int: Int, string: String)
    case class Row(nested: NestedRow)

    val record = RowParquetRecord()

    a[ParquetRecordDecoder.DecodingException] should be thrownBy ParquetRecordDecoder.decode[Row](record)
  }

  it should "throw exception if input record does not match expected type" in {
    case class NestedRow(int: Int, string: String)
    case class Row(nested: NestedRow)

    val record = RowParquetRecord("nested" -> ListParquetRecord("a", "b", "c"))

    a[ParquetRecordDecoder.DecodingException] should be thrownBy ParquetRecordDecoder.decode[Row](record)
  }

  "Collection of products decoder" should "decode record containing optional nested record" in {
    case class NestedRow(int: Int)
    case class Row(nestedOptional: Option[NestedRow])

    val dataWithSome = Row(Some(NestedRow(1)))
    val dataWithNone = Row(None)

    val record = RowParquetRecord()
    ParquetRecordDecoder.decode[Row](record) should be(dataWithNone)

    record.add("nestedOptional", RowParquetRecord("int" -> 1))
    ParquetRecordDecoder.decode[Row](record) should be(dataWithSome)
  }

  it should "decode record containing sequence of nested records" in {
    case class NestedRow(int: Int)
    case class Row(sequence: Seq[NestedRow])

    val data = Row(Seq(NestedRow(1), NestedRow(2), NestedRow(3)))

    val listParquetRecord = ListParquetRecord(
      RowParquetRecord("int" -> 1),
      RowParquetRecord("int" -> 2),
      RowParquetRecord("int" -> 3)
    )

    val record = RowParquetRecord("sequence" -> listParquetRecord)

    ParquetRecordDecoder.decode[Row](record) should be(data)
  }

  it should "decode record containing list of nested records" in {
    case class NestedRow(int: Int)
    case class Row(list: List[NestedRow])

    val data = Row(List(NestedRow(1), NestedRow(2), NestedRow(3)))

    val listParquetRecord = ListParquetRecord(
      RowParquetRecord("int" -> 1),
      RowParquetRecord("int" -> 2),
      RowParquetRecord("int" -> 3)
    )

    val record = RowParquetRecord("list" -> listParquetRecord)

    ParquetRecordDecoder.decode[Row](record) should be(data)
  }

  it should "decode record containing array of nested records" in {
    case class NestedRow(int: Int)
    case class Row(array: Array[NestedRow])

    val data = Row(Array(NestedRow(1), NestedRow(2), NestedRow(3)))

    val listParquetRecord = ListParquetRecord(
      RowParquetRecord("int" -> 1),
      RowParquetRecord("int" -> 2),
      RowParquetRecord("int" -> 3)
    )

    val record = RowParquetRecord("array" -> listParquetRecord)

    ParquetRecordDecoder.decode[Row](record).array should be(data.array)
  }

  it should "decode record containing vector of nested records" in {
    case class NestedRow(int: Int)
    case class Row(vector: Vector[NestedRow])

    val data = Row(Vector(NestedRow(1), NestedRow(2), NestedRow(3)))

    val listParquetRecord = ListParquetRecord(
      RowParquetRecord("int" -> 1),
      RowParquetRecord("int" -> 2),
      RowParquetRecord("int" -> 3)
    )

    val record = RowParquetRecord("vector" -> listParquetRecord)

    ParquetRecordDecoder.decode[Row](record) should be(data)
  }

  it should "decode record containing set of nested records" in {
    case class NestedRow(int: Int)
    case class Row(set: Set[NestedRow])

    val data = Row(Set(NestedRow(1), NestedRow(2), NestedRow(3)))

    val listParquetRecord = ListParquetRecord(
      RowParquetRecord("int" -> 1),
      RowParquetRecord("int" -> 2),
      RowParquetRecord("int" -> 3)
    )

    val record = RowParquetRecord("set" -> listParquetRecord)

    ParquetRecordDecoder.decode[Row](record) should be(data)
  }

  it should "throw exception if input does not match expected type" in {
    case class NestedRow(int: Int)
    case class Row(set: Set[NestedRow])

    val record = RowParquetRecord("set" -> MapParquetRecord("a" -> 1))

    a[DecodingException] should be thrownBy ParquetRecordDecoder.decode[Row](record)
  }

}
