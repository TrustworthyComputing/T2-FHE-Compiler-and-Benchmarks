package org.twc.terminator;

import org.junit.jupiter.api.BeforeAll;
import org.twc.terminator.t2dsl_compiler.T2_Compiler;

public abstract class FloatingPointTest extends T2Test {

  /**
   * Before running the tests, get a list of the input files to compile
   */
  @BeforeAll
  public void initT2TestFiles() {
    t2_files.add(new Triplet<>(
            "./src/test/resources/tests/ckks_test.t2",
            "", 0));
  }

  /**
   * Before running the tests, get a list of the input files to compile
   */
  public abstract T2_Compiler getT2Compiler(SymbolTable symbol_table,
                                            String config, int word_sz);

}
