package xyz.xzaslxr.utils.setting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class PropertyTreeNode {

    private String currentClassName;

    private String parentClassFieldName;

    private List<PropertyTreeNode> propertyTreeNodes;

    public PropertyTreeNode(String currentClassName, String parentClassFieldName) {
        this.currentClassName = currentClassName;
        this.parentClassFieldName = parentClassFieldName;
        this.propertyTreeNodes = new ArrayList<PropertyTreeNode>();
    }

    public String getCurrentClassName() {
        return currentClassName;
    }

    public void setCurrentClassName(String currentClassName) {
        this.currentClassName = currentClassName;
    }

    public String getParentClassFieldName() {
        return parentClassFieldName;
    }

    public void setParentClassFieldName(String parentClassFieldName) {
        this.parentClassFieldName = parentClassFieldName;
    }

    public void setPropertyTreeNodes(List<PropertyTreeNode> propertyTreeNodes) {
        this.propertyTreeNodes = propertyTreeNodes;
    }

    public List<PropertyTreeNode> getPropertyTreeNodes() {
        return propertyTreeNodes;
    }


    public static void main(String[] args) throws JsonProcessingException {
        PropertyTreeNode root = new PropertyTreeNode("SourceClass", "NULL");

        PropertyTreeNode rootChildrenOne = new PropertyTreeNode("FirstChild", "OneField");

        PropertyTreeNode rootChildrenTwo = new PropertyTreeNode("SecondChild", "TwoField");

        PropertyTreeNode rootGrandChildOneOne = new PropertyTreeNode("GrandChildOne", "Grand");

        PropertyTreeNode rootGrandChildTwoTwo = new PropertyTreeNode("GrandChildTwo", "Grand");

        root.getPropertyTreeNodes().add(rootChildrenOne);

        root.getPropertyTreeNodes().add(rootChildrenTwo);


        rootChildrenOne.getPropertyTreeNodes().add(rootGrandChildOneOne);

        rootChildrenTwo.getPropertyTreeNodes().add(rootGrandChildTwoTwo);


        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(root);
        System.out.println(json);
    }

}
