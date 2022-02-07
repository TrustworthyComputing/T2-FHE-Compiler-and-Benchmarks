package org.twc.terminator;

import org.junit.jupiter.api.TestInstance;
import org.twc.terminator.t2dsl_compiler.T2_2_SEAL_CKKS;
import org.twc.terminator.t2dsl_compiler.T2_Compiler;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SEALCKKSTest extends FloatingPointTest {

  public T2_Compiler getT2Compiler(SymbolTable symbol_table,
                                   String config, int word_sz) {
    return new T2_2_SEAL_CKKS(symbol_table, config);
  }

}
