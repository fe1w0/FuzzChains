# OtherTools

## diff.sh

### SetUp

```bash
# SerializationDumper.jar
git clone https://github.com/NickstaDB/SerializationDumper.git
cd SerializationDumper
build.sh

# 修改 diff.sh 中的 SerDumpJar
micro diff.sh

# 推荐使用 wdiff
sudo apt install wdiff
```

### Usage
Output:
```bash
STREAM_MAGIC - 0xac ed
STREAM_VERSION - 0x00 05
Contents
  TC_OBJECT - 0x73
    TC_CLASSDESC - 0x72
      className
        Length - 33 - 0x00 21
        Value - sources.serialize.UnsafeSerialize - 0x736f75726365732e73657269616c697a652e556e7361666553657269616c697a65
      serialVersionUID - 0x7d b5 95 64 df 4d 8f ff
      newHandle 0x00 7e 00 00
      classDescFlags - 0x03 - SC_WRITE_METHOD | SC_SERIALIZABLE
      fieldCount - 3 - 0x00 03
      Fields
        0:
          Object - L - 0x4c
          fieldName
            Length - 8 - 0x00 08
            Value - chainOne - 0x636861696e4f6e65
          className1
            TC_STRING - 0x74
              newHandle 0x00 7e 00 01
              Length - 24 - 0x00 18
              Value - Lsources/demo/SafeClass; - 0x4c736f75726365732f64656d6f2f53616665436c6173733b
        1:
          Object - L - 0x4c
          fieldName
            Length - 8 - 0x00 08
            Value - chainTwo - 0x636861696e54776f
          className1
            TC_REFERENCE - 0x71
              Handle - 8257537 - 0x00 7e 00 01
        2:
          Object - L - 0x4c
          fieldName
            Length - 4 - 0x00 04
            Value - name - 0x6e616d65
          className1
            TC_STRING - 0x74
              newHandle 0x00 7e 00 02
              Length - 18 - 0x00 12
              Value - Ljava/lang/String; - 0x4c6a6176612f6c616e672f537472696e673b
      classAnnotations
        TC_ENDBLOCKDATA - 0x78
      superClassDesc
        TC_NULL - 0x70
    newHandle 0x00 7e 00 03
    classdata
      sources.serialize.UnsafeSerialize
        values
          chainOne
            (object)
              TC_OBJECT - 0x73
                TC_CLASSDESC - 0x72
                  className
                    Length - 19 - 0x00 13
                    Value - sources.demo.ExpOne - 0x736f75726365732e64656d6f2e4578704f6e65
                  serialVersionUID - 0x0e ec 75 24 f3 03 fa 61
                  newHandle 0x00 7e 00 04
                  classDescFlags - 0x02 - SC_SERIALIZABLE
                  fieldCount - 2 - 0x00 02
                  Fields
                    0:
                      Object - L - 0x4c
                      fieldName
                        Length - 8 - 0x00 08
                        Value - chainTwo - 0x636861696e54776f
                      className1
                        TC_REFERENCE - 0x71
                          Handle - 8257537 - 0x00 7e 00 01
                    1:
                      Object - L - 0x4c
                      fieldName
                        Length - 4 - 0x00 04
                        Value - size - 0x73697a65
                      className1
                        TC_STRING - 0x74
                          newHandle 0x00 7e 00 05
                          Length - 19 - 0x00 13
                          Value - Ljava/lang/Integer; - 0x4c6a6176612f6c616e672f496e74656765723b
                  classAnnotations
                    TC_ENDBLOCKDATA - 0x78
                  superClassDesc
                    TC_CLASSDESC - 0x72
                      className
                        Length - 20 - 0x00 14
                        Value - sources.demo.SafeOne - 0x736f75726365732e64656d6f2e536166654f6e65
                      serialVersionUID - 0xbf 4e e1 28 63 2e f5 2a
                      newHandle 0x00 7e 00 06
                      classDescFlags - 0x02 - SC_SERIALIZABLE
                      fieldCount - 0 - 0x00 00
                      classAnnotations
                        TC_ENDBLOCKDATA - 0x78
                      superClassDesc
                        TC_CLASSDESC - 0x72
                          className
                            Length - 22 - 0x00 16
                            Value - sources.demo.SafeClass - 0x736f75726365732e64656d6f2e53616665436c617373
                          serialVersionUID - 0x4f 2c 49 51 d9 47 f6 c5
                          newHandle 0x00 7e 00 07
                          classDescFlags - 0x02 - SC_SERIALIZABLE
                          fieldCount - 1 - 0x00 01
                          Fields
                            0:
                              Object - L - 0x4c
                              fieldName
                                Length - 4 - 0x00 04
                                Value - name - 0x6e616d65
                              className1
                                TC_REFERENCE - 0x71
                                  Handle - 8257538 - 0x00 7e 00 02
                          classAnnotations
                            TC_ENDBLOCKDATA - 0x78
                          superClassDesc
                            TC_NULL - 0x70
                newHandle 0x00 7e 00 08
                classdata
                  sources.demo.SafeClass
                    values
                      name
                        (object)
                          TC_NULL - 0x70
                  sources.demo.SafeOne
                    values
                  sources.demo.ExpOne
                    values
                      chainTwo
                        (object)
                          TC_NULL - 0x70
                      size
                        (object)
                          TC_OBJECT - 0x73
                            TC_CLASSDESC - 0x72
                              className
                                Length - 17 - 0x00 11
                                Value - java.lang.Integer - 0x6a6176612e6c616e672e496e7465676572
                              serialVersionUID - 0x12 e2 a0 a4 f7 81 87 38
                              newHandle 0x00 7e 00 09
                              classDescFlags - 0x02 - SC_SERIALIZABLE
                              fieldCount - 1 - 0x00 01
                              Fields
                                0:
                                  Int - I - 0x49
                                  fieldName
                                    Length - 5 - 0x00 05
                                    Value - value - 0x76616c7565
                              classAnnotations
                                TC_ENDBLOCKDATA - 0x78
                              superClassDesc
                                TC_CLASSDESC - 0x72
                                  className
                                    Length - 16 - 0x00 10
                                    Value - java.lang.Number - 0x6a6176612e6c616e672e4e756d626572
                                  serialVersionUID - 0x86 ac 95 1d 0b 94 e0 8b
                                  newHandle 0x00 7e 00 0a
                                  classDescFlags - 0x02 - SC_SERIALIZABLE
                                  fieldCount - 0 - 0x00 00
                                  classAnnotations
                                    TC_ENDBLOCKDATA - 0x78
                                  superClassDesc
                                    TC_NULL - 0x70
                            newHandle 0x00 7e 00 0b
                            classdata
                              java.lang.Number
                                values
                              java.lang.Integer
                                values
                                  value
                                    [-(int)80-]
                                    {+(int)20+} - 0x00 00 00 [-50-] {+14+}
          chainTwo
            (object)
              TC_OBJECT - 0x73
                TC_CLASSDESC - 0x72
                  className
                    Length - 19 - 0x00 13
                    Value - sources.demo.ExpTwo - 0x736f75726365732e64656d6f2e45787054776f
                  serialVersionUID - 0x9b 43 33 d8 f9 a9 4a 9b
                  newHandle 0x00 7e 00 0c
                  classDescFlags - 0x02 - SC_SERIALIZABLE
                  fieldCount - 0 - 0x00 00
                  classAnnotations
                    TC_ENDBLOCKDATA - 0x78
                  superClassDesc
                    TC_CLASSDESC - 0x72
                      className
                        Length - 20 - 0x00 14
                        Value - sources.demo.SafeTwo - 0x736f75726365732e64656d6f2e5361666554776f
                      serialVersionUID - 0x9a fd 7e e6 f2 81 08 9c
                      newHandle 0x00 7e 00 0d
                      classDescFlags - 0x02 - SC_SERIALIZABLE
                      fieldCount - 0 - 0x00 00
                      classAnnotations
                        TC_ENDBLOCKDATA - 0x78
                      superClassDesc
                        TC_REFERENCE - 0x71
                          Handle - 8257543 - 0x00 7e 00 07
                newHandle 0x00 7e 00 0e
                classdata
                  sources.demo.SafeClass
                    values
                      name
                        (object)
                          TC_NULL - 0x70
                  sources.demo.SafeTwo
                    values
                  sources.demo.ExpTwo
                    values
          name
            (object)
              TC_NULL - 0x70
        objectAnnotation
          TC_ENDBLOCKDATA - 0x78
```