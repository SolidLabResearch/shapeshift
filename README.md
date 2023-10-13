# shapeshift

A Kotlin library to transform SHACL shapes into GraphQL types

## Usage

The main entry point of the package is the ```SHACLToGraphQL``` object which provides the ```getShema(config : ShiftConfig)``` function, which takes a ShiftConfig object as a paremeter and returns a GraphQL schema as a String.

### ShiftConfig
The ```ShiftConfig``` object provides the necessary configuration options. It's constructor has three parameters:
* ```catalogUrl (String)```: a URL for a Shape Catalogue to be used to retrieve shapes from. If a URL is provided, it will first try to resolve shapes from the catalog. (default = null)
* ```strict (Boolean)```: Wether it should only accept SHACL core features.
* ```shapeConfigs (Map<String, ShapeConfig>)```: A map of all ```ShapeConfigs``` (see below) to be considered, with the keys representing the URI of the Shape.

### Shape Config
This object provides additional configuration objects for each Shape to be considered. It's constructor has one parameter:
* ```mutation (Boolean)```: Wether or not mutations should be generated for the shapes.