
rid = [:]

// Clay
rid["FIRE-CLAY"] = 847
rid["SILTY-CLAY"] = 846
rid["SANDY-CLAY"] = 845
rid["CLAY-LOAM"] = 844
rid["CLAY"] = 779

// Gas
rid["SULFUR-DIOXIDE"] = 874
rid["CARBON-DIOXIDE"] = 873
rid["POLLUTED-OXYGEN"] = 871
rid["OXYGEN"] = 869
rid["VACUUM"] = 868

// Igneous-Extrusive
rid["RHYOLITE"] = 812
rid["OBSIDIAN"] = 811
rid["DACITE"] = 810
rid["BASALT"] = 809
rid["ANDESITE"] = 808

// Igneous-Intrusive
rid["GRANITE"] = 806
rid["GABBRO"] = 805
rid["DIORITE"] = 804

// Metamorphic
rid["SLATE"] = 819
rid["SCHIST"] = 818
rid["QUARTZITE"] = 817
rid["PHYLLITE"] = 816
rid["MARBLE"] = 815
rid["GNEISS"] = 814

// Sand
rid["YELLOW-SAND"] = 852
rid["WHITE-SAND"] = 851
rid["RED-SAND"] = 850
rid["BLACK-SAND"] = 849
rid["SAND"] = 778

// Seabed
rid["CALCAREOUS-OOZE"] = 856
rid["SILICEOUS-OOZE"] = 855
rid["PELAGIC-CLAY"] = 854

// Sedimentary
rid["SILTSTONE"] = 802
rid["SHALE"] = 801
rid["SANDSTONE"] = 800
rid["ROCK-SALT"] = 799
rid["MUDSTONE"] = 798
rid["LIMESTONE"] = 797
rid["DOLOMITE"] = 796
rid["CONGLOMERATE"] = 795
rid["CLAYSTONE"] = 794
rid["CHERT"] = 793
rid["CHALK"] = 792

// Liquid
rid["MAGMA"] = 879
rid["BRINE"] = 878
rid["SEA-WATER"] = 877
rid["WATER"] = 876

// Topsoil
rid["SILT-LOAM"] = 866
rid["SILTY-CLAY-LOAM"] = 865
rid["SILT"] = 864
rid["SANDY-LOAM"] = 863
rid["SANDY-CLAY-LOAM"] = 862
rid["PEAT"] = 861
rid["LOAMY-SAND"] = 860
rid["LOAM"] = 859

// Wood
rid["BIRCH-WOOD"] = 1098
rid["PINE-WOOD"] = 1090

// Grass
rid["RED-POPPY"] = 1079
rid["DAISY"] = 1078
rid["CARROT"] = 1077
rid["WHEAT"] = 1075
rid["MEADOW-GRASS"] = 1074

// Shrub
rid["BLUEBERRIES"] = 1081

// Tree-Sapling
rid["BIRCH-SAPLING"] = 1092
rid["PINE-SAPLING"] = 1084

// Furniture
rid["FURNITURE-CHAIR"] = 1047
rid["FURNITURE-TABLE"] = 1046

// Container
rid["VIAL"] = 1045
rid["MUG"] = 1044
rid["JUG"] = 1043
rid["WATERSKIN"] = 1042
rid["BOTTLE"] = 1041
rid["LARGE-JUG"] = 1040
rid["BARREL"] = 1039
rid["BIN"] = 1038
rid["SACK"] = 1037
rid["BUCKET"] = 1036
rid["BASKET"] = 1035

// Misc-Object
rid["MECHANISM"] = 1034
rid["GIANT-SAWBLADE"] = 1033
rid["METAL-BAR"] = 1032
rid["METAL-ORE"] = 771
rid["ROCK-BLOCK"] = 1031
rid["ROCK-STONE"] = 1030
rid["WOOD-PLANK"] = 1029
rid["WOOD-LOG"] = 1028

rid["UNKNOWN"] = 0xffff

map = [
    0x000000: rid["OXYGEN"],
    0xff0000: rid["WATER"],
    0x00ffff: rid["SAND"],
    0x7F007F: rid["CLAY"],
    0x00007f: rid["PEAT"],
    0xb2b2b2: rid["SANDSTONE"],
    0x7f7f7f: rid["OBSIDIAN"],
    0x4c4c4c: rid["GRANITE"],
    0x0000ff: rid["MAGMA"],
    0x191919: rid["MARBLE"],
    0xffff00: rid["OXYGEN"],
]

return map
