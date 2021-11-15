package org.twc.terminator;

import org.twc.terminator.t2dsl_compiler.SymbolTableVisitor;
import org.twc.terminator.t2dsl_compiler.T2_2_SEAL;
import org.twc.terminator.t2dsl_compiler.TypeCheckVisitor;
import org.twc.terminator.t2dsl_compiler.T2_Compiler;
import org.twc.terminator.t2dsl_compiler.T2DSLparser.ParseException;
import org.twc.terminator.t2dsl_compiler.T2DSLparser.T2DSLParser;
import org.twc.terminator.t2dsl_compiler.T2DSLsyntaxtree.Goal;

import java.io.*;
import java.util.*;

public class Main {

  private static boolean debug_ = false;

  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("fatal error: no input files.");
      System.exit(-1);
    }
    ArrayList<String> input_files = new ArrayList<>();
    for (String arg : args) {
      if (arg.toUpperCase().equals("-DEBUG") || arg.toUpperCase().equals("--DEBUG")) {
        debug_ = true;
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
        // typechecking
        T2DSLParser dslParser = new T2DSLParser(input_stream);
        Goal t2dsl_goal = dslParser.Goal();
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

        // generate SEAL code
        T2_Compiler dsl_compiler = new T2_2_SEAL(symbol_table);
        t2dsl_goal.accept(dsl_compiler);
        String code = dsl_compiler.getASM();
        String seal_out = path + ".cpp";
        writer = new PrintWriter(seal_out);
        writer.print(code);
        writer.close();
        System.out.println(code);
        System.out.println("[ \033[0;32m \u2713 \033[0m ] SEAL code generated" +
                           " to \"" + seal_out + "\"");
        input_stream = new FileInputStream(seal_out);
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
