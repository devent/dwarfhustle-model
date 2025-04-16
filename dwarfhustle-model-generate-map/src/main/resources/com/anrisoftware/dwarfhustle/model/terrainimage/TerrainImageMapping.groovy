
rid = [:]

// Clay
rid["FIRE-CLAY"] = 846
rid["SILTY-CLAY"] = 845
rid["SANDY-CLAY"] = 844
rid["CLAY-LOAM"] = 843
rid["CLAY"] = 779

// Gas
rid["SULFUR-DIOXIDE"] = 873
rid["CARBON-DIOXIDE"] = 872
rid["POLLUTED-OXYGEN"] = 870
rid["OXYGEN"] = 868
rid["VACUUM"] = 867

// Igneous-Extrusive
rid["RHYOLITE"] = 811
rid["OBSIDIAN"] = 810
rid["DACITE"] = 809
rid["BASALT"] = 808
rid["ANDESITE"] = 807

// Igneous-Intrusive
rid["GRANITE"] = 805
rid["GABBRO"] = 804
rid["DIORITE"] = 803

// Metamorphic
rid["SLATE"] = 818
rid["SCHIST"] = 817
rid["QUARTZITE"] = 816
rid["PHYLLITE"] = 815
rid["MARBLE"] = 814
rid["GNEISS"] = 813

// Sand
rid["YELLOW-SAND"] = 851
rid["WHITE-SAND"] = 850
rid["RED-SAND"] = 849
rid["BLACK-SAND"] = 848
rid["SAND"] = 778

// Seabed
rid["CALCAREOUS-OOZE"] = 855
rid["SILICEOUS-OOZE"] = 854
rid["PELAGIC-CLAY"] = 853

// Sedimentary
rid["SILTSTONE"] = 801
rid["SHALE"] = 800
rid["SANDSTONE"] = 799
rid["ROCK-SALT"] = 798
rid["MUDSTONE"] = 797
rid["LIMESTONE"] = 796
rid["DOLOMITE"] = 795
rid["CONGLOMERATE"] = 794
rid["CLAYSTONE"] = 793
rid["CHERT"] = 792
rid["CHALK"] = 791

// Liquid
rid["MAGMA"] = 878
rid["BRINE"] = 877
rid["SEA-WATER"] = 876
rid["WATER"] = 875

// Topsoil
rid["SILT-LOAM"] = 865
rid["SILTY-CLAY-LOAM"] = 864
rid["SILT"] = 863
rid["SANDY-LOAM"] = 862
rid["SANDY-CLAY-LOAM"] = 861
rid["PEAT"] = 860
rid["LOAMY-SAND"] = 859
rid["LOAM"] = 858

// Wood
rid["BIRCH-WOOD"] = 1086
rid["PINE-WOOD"] = 1078

// Grass
rid["RED-POPPY"] = 1067
rid["DAISY"] = 1066
rid["CARROT"] = 1065
rid["WHEAT"] = 1063
rid["MEADOW-GRASS"] = 1062

// Shrub
rid["BLUEBERRIES"] = 1069

// Tree-Sapling
rid["BIRCH-SAPLING"] = 1080
rid["PINE-SAPLING"] = 1072

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
