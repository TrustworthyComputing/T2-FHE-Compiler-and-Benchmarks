package org.twc.terminator;

import org.junit.jupiter.api.BeforeAll;
import org.twc.terminator.t2dsl_compiler.T2_Compiler;

public abstract class IntegerBinaryTest extends T2Test {

  /**
   * Before running the tests, get a list of the input files to compile
   */
  @BeforeAll
  public void initT2TestFiles() {
    t2_files.add(new Triplet<>(
            "./src/test/resources/tests/arithmetic.t2",
            "", 0));
    t2_files.add(new Triplet<>(
            "./src/test/resources/tests/arrays.t2",
            "", 0));
    t2_files.add(new Triplet<>(
            "./src/test/resources/tests/batching.t2",
            "", 0));
    t2_files.add(new Triplet<>(
            "./src/test/resources/tests/bin_add_test.t2",
            "", 6));
    t2_files.add(new Triplet<>(
            "./src/test/resources/tests/bin_test.t2",
            "", 6));
    t2_files.add(new Triplet<>(
            "./src/test/resources/tests/shift.t2",
            "", 6));
    t2_files.add(new Triplet<>(
            "./src/test/resources/tests/ternary.t2",
            "", 0));
  }

  /**
   * Before running the tests, get a list of the input files to compile
   */
  public abstract T2_Compiler getT2Compiler(SymbolTable symbol_table,
                                            String config, int word_sz);

}
