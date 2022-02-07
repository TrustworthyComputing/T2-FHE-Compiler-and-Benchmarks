package org.twc.terminator;

import org.junit.jupiter.api.TestInstance;
import org.twc.terminator.t2dsl_compiler.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SEALTest extends IntegerBinaryTest {

  public T2_Compiler getT2Compiler(SymbolTable symbol_table,
                                   String config, int word_sz) {
    return new T2_2_SEAL(symbol_table, config, word_sz);
  }

}
