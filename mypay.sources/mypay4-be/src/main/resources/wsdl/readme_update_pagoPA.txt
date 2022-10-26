Warning!!!

When updating WSDL / XSD from git repository of PagoPA (https://github.com/pagopa/pagopa-api),
pay attention to a possible XJC during java generation phase, caused by a XJC bug not correctly
handling comment lines ending with character "*".

Workaround: add a space character (" ") on every comment line ending with "*".
Using Intellij, this is easily accomplished using Search-Replace feature and RegEx:
- search field: "\*\*$"
- replce field: "** "
