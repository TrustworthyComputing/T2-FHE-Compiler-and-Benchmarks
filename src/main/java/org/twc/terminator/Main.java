package org.twc.terminator;

import org.twc.terminator.t2dsl_compiler.*;
import org.twc.terminator.t2dsl_compiler.T2DSLparser.ParseException;
import org.twc.terminator.t2dsl_compiler.T2DSLparser.T2DSLParser;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.Goal;

import java.io.*;

public class Main {

  enum HE_BACKEND {
    NONE, SEAL, TFHE, PALISADE, HELIB, LATTIGO
  }

  public enum ENC_TYPE {
    NONE, ENC_INT, ENC_DOUBLE
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("fatal error: no input files.");
      System.exit(-1);
    }
    String input_file = null;
    HE_BACKEND backend_ = HE_BACKEND.NONE;
    boolean debug_ = false, print_bin_ = false;
    int word_sz_ = 0;
    String config = "path to config";
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.equalsIgnoreCase("-DEBUG") ||
          arg.equalsIgnoreCase("--DEBUG")) {
        debug_ = true;
      } else if (arg.equalsIgnoreCase("-SEAL") ||
                 arg.equalsIgnoreCase("--SEAL")) {
        backend_ = HE_BACKEND.SEAL;
      } else if (arg.equalsIgnoreCase("-TFHE") ||
                 arg.equalsIgnoreCase("--TFHE")) {
        backend_ = HE_BACKEND.TFHE;
        if (word_sz_ == 0) word_sz_ = 8;
      } else if (arg.equalsIgnoreCase("-PALISADE") ||
                 arg.equalsIgnoreCase("--PALISADE")) {
        backend_ = HE_BACKEND.PALISADE;
      } else if (arg.equalsIgnoreCase("-HELIB") ||
                 arg.equalsIgnoreCase("--HELIB")) {
        backend_ = HE_BACKEND.HELIB;
      } else if (arg.equalsIgnoreCase("-LATTIGO") ||
                 arg.equalsIgnoreCase("--LATTIGO")) {
        backend_ = HE_BACKEND.LATTIGO;
      } else if (arg.equalsIgnoreCase("-PRINTBIN") ||
              arg.equalsIgnoreCase("--PRINTBIN")) {
        print_bin_ = true;
      } else if (arg.equalsIgnoreCase("-CONFIG") ||
                 arg.equalsIgnoreCase("--CONFIG")) {
        if (++i >= args.length) {
          System.out.println("[ \033[0;31m X \033[0m ] Configuration file " +
                             "must be passed after the -config parameter.");
          System.exit(-1);
        }
        config = args[i];
      } else if (arg.equalsIgnoreCase("-W") ||
                 arg.equalsIgnoreCase("--W")) {
        if (++i >= args.length) {
          System.out.println("[ \033[0;31m X \033[0m ] Word size " +
                             "must be passed after the -w parameter.");
          System.exit(-1);
        }
        word_sz_ = Integer.parseInt(args[i]);
      } else {
        input_file = arg;
      }
    }
    assert(input_file != null);
    InputStream input_stream = null;
    PrintWriter writer = null;
    File fp = new File(input_file);
    String path = fp.getPath();
    path = path.substring(0, path.lastIndexOf('.'));
    try {
      System.out.println("Compiling file \"" + input_file + "\"");
      input_stream = new FileInputStream(input_file);
      // Type checking.
      Goal t2dsl_goal = new T2DSLParser(input_stream).Goal();
      SymbolTableVisitor symtable_visit = new SymbolTableVisitor();
      t2dsl_goal.accept(symtable_visit);
      SymbolTable symbol_table = symtable_visit.getSymbolTable();
      if (debug_) {
        System.out.println();
        symtable_visit.printSymbolTable();
      }
      System.out.println("[ 1/2 ] Class members and methods info collection" +
                          " phase completed");
      TypeCheckVisitor type_checker = new TypeCheckVisitor(symbol_table);
      t2dsl_goal.accept(type_checker);
      ENC_TYPE scheme_ = type_checker.getScheme();
      if (scheme_ == ENC_TYPE.ENC_DOUBLE && word_sz_ > 0) {
        throw new RuntimeException("Cannot use binary with encrypted " +
                                    "double type.");
      } else if (scheme_ == ENC_TYPE.NONE) {
        scheme_ = ENC_TYPE.ENC_INT;
      }
      System.out.println("[ 2/2 ] Type checking phase completed");
      System.out.println("[ \033[0;32m \u2713 \033[0m ] All checks passed");
      System.out.print("[ \033[1;33m ! \033[0m ] Using " + backend_.name());
      if (word_sz_ > 0) {
        System.out.println(" in Binary domain with word size = " + word_sz_);
      } else {
        if (scheme_ == ENC_TYPE.ENC_INT) {
          System.out.println(" in Integer domain");
        } else if (scheme_ == ENC_TYPE.ENC_DOUBLE) {
          System.out.println(" in Floating-Point domain");
        }
      }
      if (print_bin_ && word_sz_ == 0 && !backend_.equals(HE_BACKEND.TFHE)) {
        System.out.println("[ \033[1;33m ! \033[0m ] PRINTBIN option is only " +
            "supported by TFHE. Use the binary\n        domain by setting " +
            "the word size parameter (e.g., --w 8)");
      }
      // Code generation.
      T2_Compiler dsl_compiler = null;
      String suffix = ".cpp";
      switch (backend_) {
        case NONE:
          throw new RuntimeException("Provide a backend (i.e., -SEAL, " +
                                     "-TFHE, -PALISADE, -HELIB, -LATTIGO)");
        case SEAL:
          if (scheme_ == ENC_TYPE.ENC_INT) {
            dsl_compiler = new T2_2_SEAL(symbol_table, config, word_sz_);
          } else if (scheme_ == ENC_TYPE.ENC_DOUBLE) {
            dsl_compiler = new T2_2_SEAL_CKKS(symbol_table, config);
          }
          break;
        case TFHE:
          dsl_compiler = new T2_2_TFHE(symbol_table, config, word_sz_, print_bin_);
          break;
        case PALISADE:
          if (scheme_ == ENC_TYPE.ENC_INT) {
            dsl_compiler = new T2_2_PALISADE(symbol_table, config, word_sz_);
          } else if (scheme_ == ENC_TYPE.ENC_DOUBLE) {
            dsl_compiler = new T2_2_PALISADE_CKKS(symbol_table, config);
          }
          break;
        case HELIB:
          if (scheme_ == ENC_TYPE.ENC_INT) {
            dsl_compiler = new T2_2_HElib(symbol_table, config, word_sz_);
          } else if (scheme_ == ENC_TYPE.ENC_DOUBLE) {
            dsl_compiler = new T2_2_HElib_CKKS(symbol_table, config);
          }
          break;
        case LATTIGO:
          suffix = ".go";
          if (scheme_ == ENC_TYPE.ENC_INT) {
            dsl_compiler = new T2_2_Lattigo(symbol_table, config, word_sz_);
          } else if (scheme_ == ENC_TYPE.ENC_DOUBLE) {
            dsl_compiler = new T2_2_Lattigo_CKKS(symbol_table, config);
          }
          break;
        default:
          throw new RuntimeException("Backend is not supported yet");
      }
      if (dsl_compiler == null) {
        throw new RuntimeException("Combination of backend and scheme is " +
                                   "not supported.");
      }
      t2dsl_goal.accept(dsl_compiler);
      String code = dsl_compiler.get_asm();
      String output_path = path + suffix;
      writer = new PrintWriter(output_path);
      writer.print(code);
      writer.close();
      if (debug_) System.out.println(code);
      System.out.println("[ \033[0;32m \u2713 \033[0m ] " + backend_.name() +
                         " code generated to \"" + output_path + "\"");
      input_stream = new FileInputStream(output_path);
    } catch (ParseException | FileNotFoundException ex) {
      ex.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(-1);
    } finally {
      try {
        if (input_stream != null) input_stream.close();
        if (writer != null) writer.close();
      } catch (IOException ex) {
        ex.printStackTrace();
        System.exit(-1);
      }
    }
  }
}
