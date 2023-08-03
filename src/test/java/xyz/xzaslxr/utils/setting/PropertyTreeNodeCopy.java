package xyz.xzaslxr.utils.setting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */

public class PropertyTreeNodeCopy {

    private String currentClassName;

    private String parentClassFieldName;

    private  String nodeLabel;

    private List<PropertyTreeNodeCopy> propertyTreeNodes;

    public PropertyTreeNodeCopy(String currentClassName, String parentClassFieldName, String nodeLabel) {
        this.currentClassName = currentClassName;
        this.parentClassFieldName = parentClassFieldName;
        this.nodeLabel = nodeLabel;
        this.propertyTreeNodes = new ArrayList<PropertyTreeNodeCopy>();
    }

    public PropertyTreeNodeCopy(String currentClassName, String parentClassFieldName) {
        this.currentClassName = currentClassName;
        this.parentClassFieldName = parentClassFieldName;
        // 表示当前node为leaf
        this.nodeLabel = null;
        this.propertyTreeNodes = new ArrayList<PropertyTreeNodeCopy>();
    }

    public String getCurrentClassName() {
        return currentClassName;
    }

    public void setCurrentClaspropertyTreeNodesName(String currentClassName) {
        this.currentClassName = currentClassName;
    }

    public String getParentClassFieldName() {
        return parentClassFieldName;
    }

    public void setParentClassFieldName(String parentClassFieldName) {
        this.parentClassFieldName = parentClassFieldName;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public void setPropertyTreeNodes(List<PropertyTreeNodeCopy> propertyTreeNodes) {
        this.propertyTreeNodes = propertyTreeNodes;
    }

    public List<PropertyTreeNodeCopy> getPropertyTreeNodes() {
        return propertyTreeNodes;
    }


    public static void main(String[] args) throws JsonProcessingException {
        PropertyTreeNodeCopy root = new PropertyTreeNodeCopy("SourceClass", "NULL");

        PropertyTreeNodeCopy rootChildrenOne = new PropertyTreeNodeCopy("FirstChild", "OneField");

        PropertyTreeNodeCopy rootChildrenTwo = new PropertyTreeNodeCopy("SecondChild", "TwoField");

        PropertyTreeNodeCopy rootGrandChildOneOne = new PropertyTreeNodeCopy("GrandChildOne", "Grand");

        PropertyTreeNodeCopy rootGrandChildTwoTwo = new PropertyTreeNodeCopy("GrandChildTwo", "Grand");

        root.getPropertyTreeNodes().add(rootChildrenOne);

        root.getPropertyTreeNodes().add(rootChildrenTwo);


        rootChildrenOne.getPropertyTreeNodes().add(rootGrandChildOneOne);

        rootChildrenTwo.getPropertyTreeNodes().add(rootGrandChildTwoTwo);


        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(root);
        System.out.println(json);
    }

}
