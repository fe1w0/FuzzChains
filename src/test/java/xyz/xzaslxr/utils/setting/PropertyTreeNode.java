package xyz.xzaslxr.utils.setting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 *
 */

public class PropertyTreeNode {

    /**
     * Node的label分为三类:
     * <p>1. ROOT </p>
     * <p>2. ORDINARY</p>
     * <p>3. PRIORITY</p>
     */
    private String label;

    private String className;

    private  String fieldName;

    private List<PropertyTreeNode> fields;



    public PropertyTreeNode(String label, String className, String fieldName, List<PropertyTreeNode> fields) {
        this.label = label;
        this.className = className;
        this.fieldName = fieldName;
        this.fields = fields;
    }


    /**
     * 初始化 PropertyTreeNode.
     * <p>label: "Root"</p>
     * @param label
     * @param className
     * @param fieldName
     */
    public PropertyTreeNode(String label, String className, String fieldName) {
        this.label = label;
        this.className = className;
        this.fieldName = fieldName;
        this.fields = new ArrayList<PropertyTreeNode>();
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public List<PropertyTreeNode> getFields() {
        return fields;
    }

    public void setFields(List<PropertyTreeNode> fields) {
        this.fields = fields;
    }

    public static void main(String[] args) throws JsonProcessingException {

        PropertyTreeNode sizeField = new PropertyTreeNode("PRIORITY", "java.lang.Integer", "size");

        List<PropertyTreeNode> tmpFieldList = new ArrayList<>();
        tmpFieldList.add(sizeField);

        PropertyTreeNode chainOne = new PropertyTreeNode("ORDINARY", "sources.demo.ExpOne", "chainOne", tmpFieldList);

        PropertyTreeNode chainTwo = new PropertyTreeNode("ORDINARY", "sources.demo.ExpTwo", "chainTwo");

        List<PropertyTreeNode> newTmpFieldList = new ArrayList<>();
        newTmpFieldList.add(chainOne);
        newTmpFieldList.add(chainTwo);

        PropertyTreeNode root = new PropertyTreeNode("ROOT", "sources.serialize.UnsafeSerialize", null, newTmpFieldList);


        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(root);
        System.out.println(json);

    }



}
