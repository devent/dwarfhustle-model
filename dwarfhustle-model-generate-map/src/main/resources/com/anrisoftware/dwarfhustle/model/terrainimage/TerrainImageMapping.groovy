
rid = [:]

// Clay
rid["FIRE-CLAY"] = 845
rid["SILTY-CLAY"] = 844
rid["SANDY-CLAY"] = 843
rid["CLAY-LOAM"] = 842
rid["CLAY"] = 779

// Gas
rid["SULFUR-DIOXIDE"] = 872
rid["CARBON-DIOXIDE"] = 871
rid["POLLUTED-OXYGEN"] = 869
rid["OXYGEN"] = 867
rid["VACUUM"] = 866

// Igneous-Extrusive
rid["RHYOLITE"] = 810
rid["OBSIDIAN"] = 809
rid["DACITE"] = 808
rid["BASALT"] = 807
rid["ANDESITE"] = 806

// Igneous-Intrusive
rid["GRANITE"] = 804
rid["GABBRO"] = 803
rid["DIORITE"] = 802

// Metamorphic
rid["SLATE"] = 817
rid["SCHIST"] = 816
rid["QUARTZITE"] = 815
rid["PHYLLITE"] = 814
rid["MARBLE"] = 813
rid["GNEISS"] = 812

// Sand
rid["YELLOW-SAND"] = 850
rid["WHITE-SAND"] = 849
rid["RED-SAND"] = 848
rid["BLACK-SAND"] = 847
rid["SAND"] = 778

// Seabed
rid["CALCAREOUS-OOZE"] = 854
rid["SILICEOUS-OOZE"] = 853
rid["PELAGIC-CLAY"] = 852

// Sedimentary
rid["SILTSTONE"] = 800
rid["SHALE"] = 799
rid["SANDSTONE"] = 798
rid["ROCK-SALT"] = 797
rid["MUDSTONE"] = 796
rid["LIMESTONE"] = 795
rid["DOLOMITE"] = 794
rid["CONGLOMERATE"] = 793
rid["CLAYSTONE"] = 792
rid["CHERT"] = 791
rid["CHALK"] = 790

// Liquid
rid["MAGMA"] = 875
rid["WATER"] = 874

// Topsoil
rid["SILT-LOAM"] = 864
rid["SILTY-CLAY-LOAM"] = 863
rid["SILT"] = 862
rid["SANDY-LOAM"] = 861
rid["SANDY-CLAY-LOAM"] = 860
rid["PEAT"] = 859
rid["LOAMY-SAND"] = 858
rid["LOAM"] = 857

// Wood
rid["BIRCH-WOOD"] = 1043
rid["PINE-WOOD"] = 1035

// Grass
rid["RED-POPPY"] = 1024
rid["DAISY"] = 1023
rid["CARROT"] = 1022
rid["WHEAT"] = 1020
rid["MEADOW-GRASS"] = 1019

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
