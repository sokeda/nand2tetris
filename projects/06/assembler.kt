import java.io.File

class Code() {
  companion object {
    fun dest(nimonic: String): String {
      var d = ""
      d += if (nimonic.contains("A")) {
        "1"
      } else {
        "0"
      }
      d += if (nimonic.contains("D")) {
        "1"
      } else {
        "0"
      }
      d += if (nimonic.contains("M")) {
        "1"
      } else {
        "0"
      }
      return d
    }

    fun comp(nimonic: String): String {
      val map = mapOf(
        "0" to "0101010",
        "1" to "0111111",
        "-1" to "0111010",
        "D" to "0001100",
        "A" to "0110000",
        "!D" to "0001101",
        "!A" to "0110001",
        "-D" to "0001111",
        "-A" to "0110011",
        "D+1" to "0011111",
        "A+1" to "0110111",
        "D-1" to "0001110",
        "A-1" to "0110010",
        "D+A" to "0000010",
        "D-A" to "0010011",
        "A-D" to "0000111",
        "D&A" to "0000000",
        "D|A" to "0010101",
        "M" to "1110000",
        "!M" to "1110001",
        "-M" to "1110011",
        "M+1" to "1110111",
        "M-1" to "1110010",
        "D+M" to "1000010",
        "D-M" to "1010011",
        "M-D" to "1000111",
        "D&M" to "1000000",
        "D|M" to "1010101"
      )
      return map.get(nimonic)!!
    }

    fun jump(nimonic: String): String {
      return when (nimonic) {
        "JGT" -> "001"
        "JEQ" -> "010"
        "JGE" -> "011"
        "JLT" -> "100"
        "JNE" -> "101"
        "JLE" -> "110"
        "JMP" -> "111"
        else -> "000"
      }
    }

  }
}

class SymbolTable() {
  var table = mutableMapOf<String, Int>()

  init {
    table.putAll(mapOf(
      "R0" to 0,
      "R1" to 1,
      "R2" to 2,
      "R3" to 3,
      "R4" to 4,
      "R5" to 5,
      "R6" to 6,
      "R7" to 7,
      "R8" to 8,
      "R9" to 9,
      "R10" to 10,
      "R11" to 11,
      "R12" to 12,
      "R13" to 13,
      "R14" to 14,
      "R15" to 15,
      "SP" to 0,
      "LCL" to 1,
      "ARG" to 2,
      "THIS" to 3,
      "THAT" to 4,
      "SCREEN" to 16384,
      "KBD" to 24576
    ))
  }

  var index = 16 // 保存開始アドレス

  fun addEntry(convert: String, address: Int) {
    table[convert] = address
  }

  fun addEntry(convert: String) {
    table[convert] = this.index++
  }

  fun contains(convert: String): Boolean {
    return table.contains(convert)
  }

  fun getAddress(convert: String): Int {
    return table.get(convert)!!
  }
}

sealed class Command{
  abstract fun convert(symbolTable: SymbolTable)
  abstract fun saveSymbolForL(binaryIndex: Int, symbolTable: SymbolTable): Int
  abstract fun symbol(): String

  class A(private var input: String): Command(){
    override fun convert(symbolTable: SymbolTable) {
      val symbol = symbol()
      // シンボルに文字が含まれる場合
      val address = if (symbol.contains(Regex("""\D+"""))) {
          // シンボルテーブルに含まれていない場合
          if (!symbolTable.contains(symbol)) {
            // 新たに追加
            symbolTable.addEntry(symbol)
          }
          // シンボルテーブルからアドレスを解決
          symbolTable.getAddress(symbol)
        } else {
          // シンボルが数値の場合
          Integer.parseInt(symbol)
        }
      println("%016d".format(Integer.toBinaryString(address).toLong()))
    }
    override fun saveSymbolForL(binaryIndex: Int, symbolTable: SymbolTable): Int {
      return binaryIndex + 1
    }
    override fun symbol(): String {
      return input.replace("@", "").trim()
    }
  }

  class C(private var input: String): Command(){
    override fun convert(symbolTable: SymbolTable) {
      println("111" + Code.comp(comp()) + Code.dest(dest()) + Code.jump(jump()))
    }
    override fun saveSymbolForL(binaryIndex: Int, symbolTable: SymbolTable): Int {
      return binaryIndex + 1
    }
    override fun symbol(): String {
      return ""
    }

    fun dest(): String {
      if (!input.contains("=")) {
        return ""
      }
      return input.split("=")[0].trim()
    }
    fun comp(): String {
      var comp = input
      if (input.contains(";")) {
        comp = input.split(";")[0].trim()
      }
      if (input.contains("=")) {
        comp = input.split("=")[1].trim()
      }
      return comp
    }
    fun jump(): String {
      if (!input.contains(";")) {
        return ""
      }
      return input.split(";")[1].trim()
    }
  }

  class L(private var input: String): Command(){
    override fun convert(symbolTable: SymbolTable){}
    override fun saveSymbolForL(binaryIndex: Int, symbolTable: SymbolTable): Int {
      symbolTable.addEntry(symbol(), binaryIndex + 1)
      return binaryIndex
    }
    override fun symbol(): String {
      return input.replace("(", "").replace(")", "")
    }
  }
}

fun switchCommand(input: String): Command{
  return when {
    input.startsWith("@") -> Command.A(input)
    input.startsWith("(") && input.endsWith(")") -> Command.L(input)
    else -> Command.C(input)
  }
}

fun scan(lines: List<String>): List<String>{
  return lines.map{ it.split("//")[0].trim() }.filter{ it.length != 0 } //コメント・空白行を削除
}

fun main(args: Array<String>) {
  var command: Command
  val symbolTable = SymbolTable()

  val fileName = args[0]
  val lines = File(fileName).readLines()
  val inputs = scan(lines)

  var binaryIndex = -1
  for (input in inputs) {
    command = switchCommand(input)
    binaryIndex = command.saveSymbolForL(binaryIndex, symbolTable)
  }

  for (input in inputs) {
    command = switchCommand(input)
    command.convert(symbolTable)
  }
}
