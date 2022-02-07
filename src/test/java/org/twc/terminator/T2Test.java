package org.twc.terminator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.twc.terminator.t2dsl_compiler.SymbolTableVisitor;
import org.twc.terminator.t2dsl_compiler.T2DSLparser.T2DSLParser;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.Goal;
import org.twc.terminator.t2dsl_compiler.T2_Compiler;
import org.twc.terminator.t2dsl_compiler.TypeCheckVisitor;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Stream;

public abstract class T2Test {

  protected static final ArrayList<Triplet<String, String, Integer>> t2_files =
          new ArrayList<>();

  /**
   * Before running the tests, get a list of the input files to compile
   */
  public abstract void initT2TestFiles();

  /**
   * Before running the tests, get a list of the input files to compile
   */
  public abstract T2_Compiler getT2Compiler(SymbolTable symbol_table,
                                            String config, int word_sz);

  /**
   * Test all input files
   */
  @ParameterizedTest(name = "Testing {arguments}")
  @MethodSource("getT2FilesAsStream")
  public void testAllFiles(Triplet<String, String, Integer> inputs) {
    // parse inputs
    String input_file = inputs.getFst();
    String config = inputs.getSnd();
    int word_sz_ = inputs.getThd();

    StringBuilder sb = new StringBuilder();
    sb.append(input_file);
    InputStream input_stream = null;
    PrintWriter writer = null;
    File fp = new File(input_file);
    String path = fp.getPath();
    path = path.substring(0, path.lastIndexOf('.'));
    try {
      input_stream = new FileInputStream(input_file);
      Goal t2dsl_goal = new T2DSLParser(input_stream).Goal();
      SymbolTableVisitor symtable_visit = new SymbolTableVisitor();
      t2dsl_goal.accept(symtable_visit);
      SymbolTable symbol_table = symtable_visit.getSymbolTable();
      // type check
      TypeCheckVisitor type_checker = new TypeCheckVisitor(symbol_table);
      t2dsl_goal.accept(type_checker);
      // compile
      T2_Compiler dsl_compiler = getT2Compiler(symbol_table, config, word_sz_);
      t2dsl_goal.accept(dsl_compiler);
      // write output
      String code = dsl_compiler.get_asm();
      String output_path = path + ".cpp";
      writer = new PrintWriter(output_path);
      writer.print(code);
      writer.close();
      input_stream = new FileInputStream(output_path);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    } finally {
      try {
        if (input_stream != null) input_stream.close();
        if (writer != null) writer.close();
        sb.append(" \033[0;32m").append("PASSED").append("\033[0m");
        System.out.println(sb.toString());
      } catch (IOException ex) {
        ex.printStackTrace();
        System.exit(-1);
      }
    }
  }

  @AfterAll
  static void afterAllCleanUp() {
    t2_files.clear();
  }

  /**
   * Method to convert the list of T2 file names to a stream for input to
   * testAllFiles
   */
  private static Stream<Triplet<String, String, Integer>> getT2FilesAsStream() {
    return t2_files.parallelStream();
  }

}
