
rid = [:]

// Clay
rid["FIRE-CLAY"] = 848
rid["SILTY-CLAY"] = 847
rid["SANDY-CLAY"] = 846
rid["CLAY-LOAM"] = 845
rid["CLAY"] = 779

// Gas
rid["SULFUR-DIOXIDE"] = 875
rid["CARBON-DIOXIDE"] = 874
rid["POLLUTED-OXYGEN"] = 872
rid["OXYGEN"] = 870
rid["VACUUM"] = 869

// Igneous-Extrusive
rid["RHYOLITE"] = 813
rid["OBSIDIAN"] = 812
rid["DACITE"] = 811
rid["BASALT"] = 810
rid["ANDESITE"] = 809

// Igneous-Intrusive
rid["GRANITE"] = 807
rid["GABBRO"] = 806
rid["DIORITE"] = 805

// Metamorphic
rid["SLATE"] = 820
rid["SCHIST"] = 819
rid["QUARTZITE"] = 818
rid["PHYLLITE"] = 817
rid["MARBLE"] = 816
rid["GNEISS"] = 815

// Sand
rid["YELLOW-SAND"] = 853
rid["WHITE-SAND"] = 852
rid["RED-SAND"] = 851
rid["BLACK-SAND"] = 850
rid["SAND"] = 778

// Seabed
rid["CALCAREOUS-OOZE"] = 857
rid["SILICEOUS-OOZE"] = 856
rid["PELAGIC-CLAY"] = 855

// Sedimentary
rid["SILTSTONE"] = 803
rid["SHALE"] = 802
rid["SANDSTONE"] = 801
rid["ROCK-SALT"] = 800
rid["MUDSTONE"] = 799
rid["LIMESTONE"] = 798
rid["DOLOMITE"] = 797
rid["CONGLOMERATE"] = 796
rid["CLAYSTONE"] = 795
rid["CHERT"] = 794
rid["CHALK"] = 793

// Liquid
rid["MAGMA"] = 880
rid["BRINE"] = 879
rid["SEA-WATER"] = 878
rid["WATER"] = 877

// Topsoil
rid["SILT-LOAM"] = 867
rid["SILTY-CLAY-LOAM"] = 866
rid["SILT"] = 865
rid["SANDY-LOAM"] = 864
rid["SANDY-CLAY-LOAM"] = 863
rid["PEAT"] = 862
rid["LOAMY-SAND"] = 861
rid["LOAM"] = 860

// Wood
rid["BIRCH-WOOD"] = 1064
rid["PINE-WOOD"] = 1056

// Grass
rid["RED-POPPY"] = 1045
rid["DAISY"] = 1044
rid["CARROT"] = 1043
rid["WHEAT"] = 1041
rid["MEADOW-GRASS"] = 1040

// Shrub
rid["BLUEBERRIES"] = 1047

// Tree-Sapling
rid["BIRCH-SAPLING"] = 1058
rid["PINE-SAPLING"] = 1050

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
