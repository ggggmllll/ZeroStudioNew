package android.studio.zero.regular.expression.preview;

import android.os.Bundle;
import android.studio.zero.regular.expression.preview.model.RegexAstNode;
import android.studio.zero.regular.expression.preview.railroad.RailroadConverter;
import android.studio.zero.regular.expression.preview.railroad.RailroadDiagramView;
import android.studio.zero.regular.expression.preview.railroad.RailroadNode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 纯模拟测试 Fragment。
 * 用于验证 Railroad 渲染引擎对各种复杂节点组合的处理能力。
 */
public class RailroadDemoFragment extends Fragment {

    private RailroadDiagramView railroadView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_railroad_demo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        railroadView = view.findViewById(R.id.railroad_view_demo);

        // 生成模拟的超级复杂数据
        RegexAstNode mockAst = generateComplexMockData();
        
        // 转换并渲染
        RailroadNode root = RailroadConverter.convert(mockAst);
        railroadView.setRootNode(root);
    }

    /**
     * 手动构建一个包含所有类型节点的复杂 AST。
     * 模拟正则结构：
     * ^ (
     *    (http|https)://[\w.]+ 
     *    | 
     *    (?<mail>\w+@\w+\.\w+)
     *    |
     *    (?> \d{3}-\d{4} \s* [^a-z] )
     * )+ (?=END) $
     */
    private RegexAstNode generateComplexMockData() {
        // 1. 根序列
        RegexAstNode root = createNode(RegexAstNode.TYPE_SEQUENCE);
        root.children = new ArrayList<>();

        // Node 1: Anchor Start (^)
        root.children.add(createAnchor(1 << 4)); // Start Buf

        // Node 2: Main Loop (One or more times)
        RegexAstNode mainLoop = createNode(RegexAstNode.TYPE_QUANTIFIER);
        mainLoop.min = 1;
        mainLoop.max = -1; // Infinite
        mainLoop.greedy = true;
        
        // Loop Body: Group (Capture #1)
        RegexAstNode mainGroup = createNode(RegexAstNode.TYPE_GROUP);
        mainGroup.isCapture = true;
        mainGroup.groupNum = 1;
        
        // Group Body: Alternation (Big Choice)
        RegexAstNode alternation = createNode(RegexAstNode.TYPE_ALTERNATION);
        
        // --- Branch A: URL Pattern ---
        RegexAstNode branchA = createNode(RegexAstNode.TYPE_SEQUENCE);
        branchA.children = new ArrayList<>();
        
        // "http"
        branchA.children.add(createLiteral("http"));
        
        // "s" (Optional)
        RegexAstNode optS = createNode(RegexAstNode.TYPE_QUANTIFIER);
        optS.min = 0; optS.max = 1;
        optS.body = createLiteral("s");
        branchA.children.add(optS);
        
        // "://"
        branchA.children.add(createLiteral("://"));
        
        // [\w.]+
        RegexAstNode domain = createNode(RegexAstNode.TYPE_QUANTIFIER);
        domain.min = 1; domain.max = -1;
        domain.body = createCharset(false, "\\w", ".");
        branchA.children.add(domain);

        // --- Branch B: Email Pattern (Named Group) ---
        RegexAstNode branchB = createNode(RegexAstNode.TYPE_GROUP);
        branchB.isCapture = true;
        branchB.groupNum = 2;
        // 借用 error 字段模拟命名组显示 (实际项目中应有专门字段)
        branchB.error = "Group 'mail'"; 
        
        RegexAstNode emailSeq = createNode(RegexAstNode.TYPE_SEQUENCE);
        emailSeq.children = new ArrayList<>();
        emailSeq.children.add(createEscape(12)); // \w
        emailSeq.children.add(createNode(RegexAstNode.TYPE_QUANTIFIER)); // +
        ((RegexAstNode)emailSeq.children.get(1)).min=1; 
        ((RegexAstNode)emailSeq.children.get(1)).max=-1;
        ((RegexAstNode)emailSeq.children.get(1)).body = emailSeq.children.get(0);
        emailSeq.children.remove(0); // 修正链表
        
        emailSeq.children.add(createLiteral("@"));
        emailSeq.children.add(createNode(RegexAstNode.TYPE_ANY_CHAR)); // . (Any Char) test
        branchB.body = emailSeq;

        // --- Branch C: Stress Test (Atomic Group + Lookahead + Backref) ---
        RegexAstNode branchC = createNode(RegexAstNode.TYPE_SEQUENCE);
        branchC.children = new ArrayList<>();
        
        // Atomic Group (?>...)
        RegexAstNode atomic = createNode(RegexAstNode.TYPE_GROUP);
        atomic.isCapture = false;
        atomic.groupSubType = "atomic";
        
        RegexAstNode atomicSeq = createNode(RegexAstNode.TYPE_SEQUENCE);
        atomicSeq.children = new ArrayList<>();
        
        // \d{3}
        RegexAstNode digits = createNode(RegexAstNode.TYPE_QUANTIFIER);
        digits.min = 3; digits.max = 3;
        digits.body = createEscape(4); // \d
        atomicSeq.children.add(digits);
        
        // -
        atomicSeq.children.add(createLiteral("-"));
        
        // \d{4}
        RegexAstNode digits4 = createNode(RegexAstNode.TYPE_QUANTIFIER);
        digits4.min = 4; digits4.max = 4;
        digits4.body = createEscape(4);
        atomicSeq.children.add(digits4);
        
        atomic.body = atomicSeq;
        branchC.children.add(atomic);
        
        // \s* (Whitespace, 0 or more)
        RegexAstNode ws = createNode(RegexAstNode.TYPE_QUANTIFIER);
        ws.min = 0; ws.max = -1;
        ws.body = createEscape(9); // \s
        branchC.children.add(ws);
        
        // [^a-z] (Inverted Charset)
        branchC.children.add(createCharset(true, "a-z"));
        
        // Backreference \1
        RegexAstNode backref = createNode(RegexAstNode.TYPE_BACKREF);
        backref.backRefIndex = 1;
        branchC.children.add(backref);

        // 构建 Alternation 树 (A | (B | C))
        RegexAstNode subAlt = createNode(RegexAstNode.TYPE_ALTERNATION);
        subAlt.left = branchB;
        subAlt.right = branchC;
        
        alternation.left = branchA;
        alternation.right = subAlt;

        // 组装主结构
        mainGroup.body = alternation;
        mainLoop.body = mainGroup;
        root.children.add(mainLoop);

        // Node 3: Lookahead (?=END)
        RegexAstNode lookahead = createNode(RegexAstNode.TYPE_LOOKAROUND);
        lookahead.anchorType = 1; // Positive Lookahead
        lookahead.body = createLiteral("END");
        root.children.add(lookahead);

        // Node 4: Anchor End ($)
        root.children.add(createAnchor(1 << 7)); // End Buf

        return root;
    }

    // --- Helpers ---

    private RegexAstNode createNode(String type) {
        RegexAstNode node = new RegexAstNode();
        node.railType = type;
        return node;
    }

    private RegexAstNode createLiteral(String text) {
        RegexAstNode node = createNode(RegexAstNode.TYPE_LITERAL);
        node.text = text;
        return node;
    }

    private RegexAstNode createEscape(int escType) {
        RegexAstNode node = createNode(RegexAstNode.TYPE_ESCAPE);
        node.escType = escType;
        return node;
    }

    private RegexAstNode createAnchor(int anchorType) {
        RegexAstNode node = createNode(RegexAstNode.TYPE_ANCHOR);
        node.anchorType = anchorType;
        return node;
    }

    private RegexAstNode createCharset(boolean invert, String... ranges) {
        RegexAstNode node = createNode(RegexAstNode.TYPE_CHARSET);
        node.invert = invert;
        node.ranges = new ArrayList<>(Arrays.asList(ranges));
        return node;
    }
}