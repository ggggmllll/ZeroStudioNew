package android.studio.zero.regular.expression.preview.railroad;

import android.studio.zero.regular.expression.preview.model.RegexAstNode;
//@author android_zero
public class RailroadConverter {

    public static RailroadNode convert(RegexAstNode ast) {
        if (ast == null) return null;
        
        // 顶层结构：Start -> AST -> End
        SequenceNode mainSeq = new SequenceNode();
        mainSeq.add(new TerminalNode(true)); // 起点
        mainSeq.add(convertNode(ast));
        mainSeq.add(new TerminalNode(false)); // 终点
        
        return mainSeq;
    }

    private static RailroadNode convertNode(RegexAstNode ast) {
        if (ast == null) return null;

        switch (ast.railType) {
            case RegexAstNode.TYPE_SEQUENCE:
                SequenceNode seq = new SequenceNode();
                if (ast.children != null) {
                    for (RegexAstNode child : ast.children) {
                        seq.add(convertNode(child));
                    }
                }
                return seq;

            case RegexAstNode.TYPE_ALTERNATION:
                ChoiceNode choice = new ChoiceNode(0);
                flattenAlternation(ast, choice);
                return choice;

            case RegexAstNode.TYPE_QUANTIFIER:
                RailroadNode body = convertNode(ast.body);
                return new LoopNode(body, ast.min, ast.max, ast.greedy);

            case RegexAstNode.TYPE_LITERAL:
                return new BoxNode(escapeVisual(ast.text), null, 
                        RailroadConstants.BG_LITERAL, RailroadConstants.STROKE_LITERAL, 
                        RailroadConstants.COLOR_TEXT, false, true);

            case RegexAstNode.TYPE_ESCAPE:
                String escText = getEscapeText(ast.escType, ast.text);
                return new BoxNode(escText, null, 
                        RailroadConstants.BG_ESCAPE, RailroadConstants.STROKE_ESCAPE, 
                        RailroadConstants.COLOR_TEXT, true, false);
            
            case RegexAstNode.TYPE_BACKREF:
                return new BoxNode("Backref #" + ast.backRefIndex, null, 
                        RailroadConstants.BG_ESCAPE, RailroadConstants.STROKE_ESCAPE, 
                        RailroadConstants.COLOR_TEXT, true, false);

            case RegexAstNode.TYPE_ANY_CHAR:
                return new BoxNode("any char", null, 
                        RailroadConstants.BG_ANYCHAR, 0, 
                        android.graphics.Color.WHITE, true, false);

            case RegexAstNode.TYPE_CHARSET:
                String label = ast.invert ? "None of:" : "One of:";
                StringBuilder sb = new StringBuilder();
                if (ast.ranges != null) {
                    int count = 0;
                    for (String r : ast.ranges) {
                         if(count++ > 0) sb.append(" ");
                         sb.append(r);
                    }
                }
                if (sb.length() == 0) sb.append("Empty");
                return new BoxNode(sb.toString(), label, 
                        RailroadConstants.BG_CHARSET, RailroadConstants.STROKE_CHARSET, 
                        RailroadConstants.COLOR_TEXT, false, false);

            case RegexAstNode.TYPE_ANCHOR:
                return new BoxNode(getAnchorText(ast.anchorType), null, 
                        RailroadConstants.BG_ANCHOR, RailroadConstants.STROKE_ANCHOR, 
                        android.graphics.Color.WHITE, false, false);

            case RegexAstNode.TYPE_GROUP:
                RailroadNode groupBody = convertNode(ast.body);
                String gLabel = ast.isCapture ? "Group #" + ast.groupNum : "Cluster";
                if ("atomic".equals(ast.groupSubType)) gLabel = "Atomic Group";
                else if ("option".equals(ast.groupSubType)) gLabel = "Options";
                return new GroupNode(groupBody, gLabel, ast.isCapture);
                
            case RegexAstNode.TYPE_LOOKAROUND:
                RailroadNode laBody = convertNode(ast.body);
                return new GroupNode(laBody, getLookaroundText(ast.anchorType), false);

            default:
                return new BoxNode("?", null, RailroadConstants.BG_CHARSET, 0, RailroadConstants.COLOR_TEXT, false, false);
        }
    }
    
    // 扁平化递归的分支结构
    private static void flattenAlternation(RegexAstNode ast, ChoiceNode choice) {
        if (RegexAstNode.TYPE_ALTERNATION.equals(ast.railType)) {
            flattenAlternation(ast.left, choice);
            flattenAlternation(ast.right, choice);
        } else {
            choice.add(convertNode(ast));
        }
    }

    private static String escapeVisual(String s) {
        return s == null ? "" : s.replace("\n", "\\n").replace("\r", "\\r");
    }
    
    private static String getEscapeText(int type, String text) {
        switch (type) {
            case 4: return "Digit";
            case 9: return "WhiteSpace";
            case 12: return "Word";
            case 11: return "Hex";
        }
        if (text != null && !text.isEmpty()) return text;
        return "Esc:" + type;
    }
    
    private static String getAnchorText(int type) {
        if ((type & (1<<4)) != 0) return "Start";
        if ((type & (1<<7)) != 0) return "End";
        if ((type & (1<<10)) != 0) return "Word Bdr";
        return "Anchor";
    }
    
    private static String getLookaroundText(int type) {
        if ((type & 1) != 0) return "Positive Lookahead";
        if ((type & 2) != 0) return "Negative Lookahead";
        if ((type & 4) != 0) return "Positive Lookbehind";
        if ((type & 8) != 0) return "Negative Lookbehind";
        return "Lookaround";
    }
}