package org.twc.terminator;

import org.twc.terminator.basetype.SymbolTable;
import org.twc.terminator.hejava2spiglet.SymbolTableVisitor;
import org.twc.terminator.hejava2spiglet.TypeCheckVisitor;
import org.twc.terminator.hejava2spiglet.HEJava2Spiglet;
import org.twc.terminator.hejava2spiglet.hejavaparser.ParseException;
import org.twc.terminator.hejava2spiglet.hejavaparser.HEJavaParser;
import org.twc.terminator.hejava2spiglet.hejavasyntaxtree.Goal;

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
        // hejava typechecking
        HEJavaParser hejava_parser = new HEJavaParser(input_stream);
        Goal hejava_root = hejava_parser.Goal();
        SymbolTableVisitor symtable_visit = new SymbolTableVisitor();
        hejava_root.accept(symtable_visit);
        SymbolTable symbol_table = symtable_visit.getSymbolTable();
        if (debug_) {
          System.out.println();
          symtable_visit.printSymbolTable();
        }
        System.out.println("[ 2/3 ] Class members and methods info collection phase completed");
        TypeCheckVisitor type_checker = new TypeCheckVisitor(symbol_table);
        hejava_root.accept(type_checker);
        System.out.println("[ 3/3 ] Type checking phase completed");
        System.out.println("[ \033[0;32m \u2713 \033[0m ] All checks passed");

        // generate Spiglet code
        HEJava2Spiglet hejava2spiglet = new HEJava2Spiglet(symbol_table);
        hejava_root.accept(hejava2spiglet);
        String code = hejava2spiglet.getASM();
        String spiglet_output_path = path + ".spg";
        writer = new PrintWriter(spiglet_output_path);
        writer.print(code);
        writer.close();
        System.out.println(code);
        System.out.println("[ \033[0;32m \u2713 \033[0m ] Spiglet code generated to \"" + spiglet_output_path + "\"");
        input_stream = new FileInputStream(spiglet_output_path);
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
