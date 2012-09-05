/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.tools;

import cross.datastructures.StatsMap;
import cross.datastructures.fragments.Context;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for evaluation of constraints and evaluation of Scripts.
 *
 * @author Nils Hoffmann
 *
 */
public class EvalTools {

    private static Logger log = LoggerFactory.getLogger(EvalTools.class);
    private static ScriptEngineManager manager;
    private static ScriptEngine engine;

    public static String apply(final String s,
            final HashMap<String, Double> stats, final String lastPosition) {
        if (EvalTools.isOperator(s)) {
            EvalTools.log.debug(s + " is operator");
            if (lastPosition.equals(s)) {
                EvalTools.log.debug("Double operator detected, deleting additional operator "
                        + s);
                return "";
            }
        }
        // }else if(VariableFragment.isNumber(s)){
        // Logger.getAnonymousLogger().log(Level.WARNING,s+" Is number");
        // return s;
        // }else
        if (EvalTools.isVar(s, stats)) {
            EvalTools.log.debug(s + " is var");
            return EvalTools.value(s, stats);
        }
        return s;
    }

    public static String apply(final String s,
            final Tuple2D<Context, Context> context, final String lastPosition) {
        if (EvalTools.isOperator(s)) {
            EvalTools.log.debug(s + " is operator");
            if (lastPosition.equals(s)) {
                EvalTools.log.debug("Double operator detected, deleting additional operator "
                        + s);
                return "";
            }
        }
        // }else if(VariableFragment.isNumber(s)){
        // Logger.getAnonymousLogger().log(Level.WARNING,s+" Is number");
        // return s;
        // }else
        if (EvalTools.isVar(s, context.getFirst())) {
            EvalTools.log.debug(s + " is var");
            return EvalTools.value(s, context.getFirst());
        } else if (EvalTools.isVar(s, context.getSecond())) {
            EvalTools.log.debug(s + " is var");
            return EvalTools.value(s, context.getSecond());
        }
        return s;
    }

    public static void checkEngines() {
        final ScriptEngineManager mgr = new ScriptEngineManager();
        final List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        for (final ScriptEngineFactory factory : factories) {
            System.out.println("ScriptEngineFactory Info");
            final String engName = factory.getEngineName();
            final String engVersion = factory.getEngineVersion();
            final String langName = factory.getLanguageName();
            final String langVersion = factory.getLanguageVersion();
            System.out.printf("\tScript Engine: %s (%s)\n", engName, engVersion);
            final List<String> engNames = factory.getNames();
            for (final String name : engNames) {
                System.out.printf("\tEngine Alias: %s\n", name);
            }
            System.out.printf("\tLanguage: %s (%s)\n", langName, langVersion);
        }
    }

    public static void eq(final Object a, final Object b) {
        if (!a.equals(b)) {
            throw new ConstraintViolationException(
                    "Objects a and b are not equal!");
        }
    }

    public static void eqD(final double d, final double e, final Object caller)
            throws ConstraintViolationException {
        if (d != e) {
            throw new ConstraintViolationException("Called from "
                    + caller.toString() + ": Values are not equal: " + d + "!="
                    + e);
        }
    }

    public static void eqI(final int i, final int j, final Object caller)
            throws ConstraintViolationException {
        if (i != j) {
            throw new ConstraintViolationException("Called from "
                    + caller.toString() + ": Values are not equal :" + i + "!="
                    + j);
        }
    }

    public static Double eval(final String s, final StatsMap hm) {// FIXME need
        // to try
        // StringTokenizer and
        // manual handling of
        // tokens
        final StringTokenizer stok = new StringTokenizer(s, "[+\\-*:/^()]",
                true);
        // st.parseNumbers();
        final StringBuilder sb = new StringBuilder(s.length());
        String lastPosition = "";
        while (stok.hasMoreElements()) {
            final String str = stok.nextToken();
            final String transf = EvalTools.apply(str, hm, lastPosition);
            // EvalTools.log.debug(transf);
            sb.append(transf);
            lastPosition = str;
        }
        final ScriptEngineManager manager1 = new ScriptEngineManager();
        // checkEngines();
        final ScriptEngine engine1 = manager1.getEngineByName("JavaScript");
        // Logging.getInstance().logger.info("Initialized JavaScriptEngine!");
        EvalTools.notNull(engine1,
                "Failed to initialize script engine for JavaScript",
                EvalTools.class);
        try {
            final String scr = sb.toString();
            // EvalTools.log.debug(scr);
            final Object o = engine1.eval(scr);
            if (o instanceof Double) {
                // Logging.getInstance().logger.info("" + o);
                return (Double) o;
            } else if (o instanceof Integer) {
                return ((Integer) o).doubleValue();
            }
            // EvalTools.log.info("Eval result is of type: {}",o.getClass().
            // getCanonicalName());
        } catch (final ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Double evalContext(final String s,
            final Tuple2D<Context, Context> context) {
        // Context c1 = context.getFirst();
        // Context c2 = context.getSecond();
        final StringTokenizer stok = new StringTokenizer(s, "[+\\-*:/^()]",
                true);
        // st.parseNumbers();
        final StringBuilder sb = new StringBuilder(s.length());
        String lastPosition = "";
        while (stok.hasMoreElements()) {
            final String str = stok.nextToken();
            final String transf = EvalTools.apply(str, context, lastPosition);
            EvalTools.log.debug(transf);
            sb.append(transf);
            lastPosition = str;
        }
        if (EvalTools.manager == null) {
            EvalTools.manager = new ScriptEngineManager();
        }
        if (EvalTools.engine == null) {
            EvalTools.engine = EvalTools.manager.getEngineByName("JavaScript");
            // EvalTools.log.debug("Initialized JavaScriptEngine!");
        }

        try {
            final String scr = sb.toString();
            // EvalTools.log.debug(scr);
            final Object o = EvalTools.engine.eval(scr);
            if (o instanceof Double) {
                // EvalTools.log.debug("" + o);
                return (Double) o;
            }
        } catch (final ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void inRangeD(final double min, final double max,
            final double v, final Object caller)
            throws ConstraintViolationException {
        // for(double d:v){
        if (v < min) {
            throw new ConstraintViolationException("Called from "
                    + caller.toString()
                    + ": Value was smaller than minimum supplied: " + v + "<"
                    + min);
        }
        if (v > max) {
            throw new ConstraintViolationException("Called from "
                    + caller.toString()
                    + ": Value was greater than maximum supplied: " + v + ">"
                    + max);
        }
        // }
    }

    public static void inRangeI(final int min, final int max, final int v,
            final Object caller) throws ConstraintViolationException {
        // for(int i:v){
        if (v < min) {
            throw new ConstraintViolationException("Called from "
                    + caller.toString()
                    + ": Value was smaller than minimum supplied: " + v + "<"
                    + min);
        }
        if (v > max) {
            throw new ConstraintViolationException("Called from "
                    + caller.toString()
                    + ": Value was greater than maximum supplied: " + v + ">"
                    + max);
        }
        // }
    }

    public static void gt(final int min, final int v, final Object caller) throws ConstraintViolationException {
        if (!(v > min)) {
            throw new ConstraintViolationException("Called from " + caller.toString() + ": Value was smaller or equal than expected: " + v + "<=" + min + ". Should be greater!");
        }
    }

    public static void geq(final int min, final int v, final Object caller) throws ConstraintViolationException {
        if (!(v >= min)) {
            throw new ConstraintViolationException("Called from " + caller.toString() + ": Value was smaller than expected: " + v + "<" + min + ". Should be greater or equal!");
        }
    }

    public static void lt(final int max, final int v, final Object caller) throws ConstraintViolationException {
        if (!(v < max)) {
            throw new ConstraintViolationException("Called from " + caller.toString() + ": Value was larger or equal than expected: " + v + ">=" + max + ". Should be smaller!");
        }
    }

    public static void leq(final int max, final int v, final Object caller) throws ConstraintViolationException {
        if (!(v <= max)) {
            throw new ConstraintViolationException("Called from " + caller.toString() + ": Value was larger than expected: " + v + ">" + max + ". Should be smaller or equal!");
        }
    }

    public static boolean isNumber(final String s) {
        // REGEXP taken from java 6 api, java.lang.Double
        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex = "[\\x00-\\x20]*" + // Optional leading
                // "whitespace"
                "[+-]?(" + // Optional sign character
                "NaN|" + // "NaN" string
                "Infinity|"
                + // "Infinity" string
                // A decimal floating-point string representing a finite
                // positive
                // number without a leading sign has at most five basic pieces:
                // Digits . Digits ExponentPart FloatTypeSuffix
                // 
                // Since this method allows integer-only strings as input
                // in addition to strings of floating-point literals, the
                // two sub-patterns below are simplifications of the grammar
                // productions from the Java Language Specification, 2nd
                // edition, section 3.10.2.
                // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|"
                + // . Digits ExponentPart_opt FloatTypeSuffix_opt
                "(\\.(" + Digits + ")(" + Exp + ")?)|"
                + // Hexadecimal strings
                "(("
                + // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                "(0[xX]" + HexDigits + "(\\.)?)|"
                + // 0[xX] HexDigits_opt . HexDigits BinaryExponent
                // FloatTypeSuffix_opt
                "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")"
                + ")[pP][+-]?" + Digits + "))" + "[fFdD]?))" + "[\\x00-\\x20]*";// Optional
        // trailing
        // "whitespace"
        if (Pattern.matches(fpRegex, s)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isOperator(final String s) {
        if (s.equals("*") || s.equals("/") || s.equals("-") || s.equals("+")
                || s.equals("(") || s.equals(")") || s.equals("^")) {
            return true;
        }
        return false;
    }

    public static boolean isVar(final String s, final Context c) {
        if (s.matches("[A-Za-z_]+") && c.has(s)) {
            return true;
        }
        return false;
    }

    public static boolean isVar(final String s,
            final HashMap<String, Double> stats) {
        if (s.matches("[A-Za-z_]+") && stats.containsKey(s)) {
            return true;
        }
        return false;
    }

    public static void neq(final Object a, final Object b) {
        if (a.equals(b)) {
            throw new ConstraintViolationException("Objects a and b are equal!");
        }
    }

    public static void neqD(final double i, final double j, final Object caller)
            throws ConstraintViolationException {
        if (i == j) {
            throw new ConstraintViolationException("Called from "
                    + caller.toString()
                    + ": Values are equal, but should not be :" + i + "!=" + j);
        }
    }

    public static void neqI(final int i, final int j, final Object caller)
            throws ConstraintViolationException {
        if (i == j) {
            throw new ConstraintViolationException("Called from "
                    + caller.toString() + ": Values are equal :" + i + "!=" + j);
        }
    }

    public static void notNull(final Object o, final Object caller)
            throws ConstraintViolationException {
        final String message = "Argument was null!";
        EvalTools.notNull(o, "Called from " + caller.toString() + ": "
                + message, caller);
    }

    public static void notNull(final Object o, final String message,
            final Object caller) throws ConstraintViolationException {
        if (o == null) {
            throw new ConstraintViolationException("Called from "
                    + caller.toString() + ": " + message);
        }
    }

    public static void notNull(final Object[] o, final Object caller)
            throws ConstraintViolationException {
        int i = 0;
        for (final Object ob : o) {
            try {
                EvalTools.notNull(ob, caller);
            } catch (final ConstraintViolationException cve) {
                throw new ConstraintViolationException("Called from "
                        + caller.toString() + ": Argument " + (i + 1) + " of "
                        + o.length + " was null!");
            }
            i++;
        }
    }

    public static void isNull(final Object o, final Object caller) throws ConstraintViolationException {
        if (o != null) {
            throw new ConstraintViolationException("Called from " + caller.toString() + ": "
                    + "Argument was not null!");
        }
    }

    public static String value(final String var, final Context c) {
        if (EvalTools.isVar(var, c)) {
            EvalTools.log.debug(c.get(var.toString()).toString());
            if (c.has(var.toString())) {
                return String.valueOf(c.get(var.toString()));
            }
            return String.valueOf(Double.NaN);
        }
        return String.valueOf(Double.NaN);
    }

    public static String value(final String var,
            final HashMap<String, Double> stats) {
        if (EvalTools.isVar(var, stats)) {
            EvalTools.log.debug(stats.get(var).toString());
            if (stats.containsKey(var)) {
                return String.valueOf(stats.get(var));
            }
            return String.valueOf(Double.NaN);
        }
        return String.valueOf(Double.NaN);
    }
}
