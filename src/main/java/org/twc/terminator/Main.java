package org.twc.terminator;

import org.twc.terminator.t2dsl_compiler.*;
import org.twc.terminator.t2dsl_compiler.T2DSLparser.ParseException;
import org.twc.terminator.t2dsl_compiler.T2DSLparser.T2DSLParser;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.Goal;

import java.io.*;
import java.util.ArrayList;

enum HE_BACKEND {
  NONE, SEAL, TFHE, PALISADE
}

public class Main {

  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("fatal error: no input files.");
      System.exit(-1);
    }
    ArrayList<String> input_files = new ArrayList<>();
    HE_BACKEND backend_ = HE_BACKEND.NONE;
    boolean debug_ = false;

    for (String arg : args) {
      if (arg.equalsIgnoreCase("-DEBUG") ||
          arg.equalsIgnoreCase("--DEBUG")) {
        debug_ = true;
      } else if (arg.equalsIgnoreCase("-SEAL") ||
                 arg.equalsIgnoreCase("--SEAL")) {
        backend_ = HE_BACKEND.SEAL;
      } else if (arg.equalsIgnoreCase("-TFHE") ||
                 arg.equalsIgnoreCase("--TFHE")) {
        backend_ = HE_BACKEND.TFHE;
      } else if (arg.equalsIgnoreCase("-PALISADE") ||
                 arg.equalsIgnoreCase("--PALISADE")) {
        backend_ = HE_BACKEND.PALISADE;
      } else {
        input_files.add(arg);
      }
    }

    for (String arg : input_files) {
      InputStream input_stream = null;
      PrintWriter writer = null;
      File fp = new File(arg);
      String path = fp.getPath();
      path = path.substring(0, path.lastIndexOf('.'));
      try {
        System.out.println("===================================================================================");
        System.out.println("Compiling file \"" + arg + "\"");
        input_stream = new FileInputStream(arg);
        // Type checking.
        new T2DSLParser(input_stream);
        Goal t2dsl_goal = T2DSLParser.Goal();
        SymbolTableVisitor symtable_visit = new SymbolTableVisitor();
        t2dsl_goal.accept(symtable_visit);
        SymbolTable symbol_table = symtable_visit.getSymbolTable();
        if (debug_) {
          System.out.println();
          symtable_visit.printSymbolTable();
        }
        System.out.println("[ 2/3 ] Class members and methods info collection phase completed");
        TypeCheckVisitor type_checker = new TypeCheckVisitor(symbol_table);
        t2dsl_goal.accept(type_checker);
        System.out.println("[ 3/3 ] Type checking phase completed");
        System.out.println("[ \033[0;32m \u2713 \033[0m ] All checks passed");

        // Code generation.
        T2_Compiler dsl_compiler = null;
        switch (backend_) {
          case NONE:
            throw new RuntimeException("Provide a backend (e.g., -SEAL, " +
                                       "-TFHE, ...)");
          case SEAL:
            dsl_compiler = new T2_2_SEAL(symbol_table);
            break;
          case TFHE:
            dsl_compiler = new T2_2_TFHE(symbol_table);
            break;
          case PALISADE:
            dsl_compiler = new T2_2_PALISADE(symbol_table);
            break;
          default:
            throw new RuntimeException("Backend is not supported yet");
        }
        t2dsl_goal.accept(dsl_compiler);
        String code = dsl_compiler.get_asm();
        String output_path = path + ".cpp";
        writer = new PrintWriter(output_path);
        writer.print(code);
        writer.close();
        System.out.println(code);
        System.out.println("[ \033[0;32m \u2713 \033[0m ] SEAL code generated" +
                           " to \"" + output_path + "\"");
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
}
