
rid = [:]

// Clay
rid["FIRE-CLAY"] = 904
rid["SILTY-CLAY"] = 902
rid["SANDY-CLAY"] = 900
rid["CLAY-LOAM"] = 898
rid["CLAY"] = 779

// Gas
rid["SULFUR-DIOXIDE"] = 932
rid["CARBON-DIOXIDE"] = 931
rid["POLLUTED-OXYGEN"] = 929
rid["OXYGEN"] = 927
rid["VACUUM"] = 926

// Igneous-Extrusive
rid["RHYOLITE"] = 866
rid["OBSIDIAN"] = 865
rid["DACITE"] = 864
rid["BASALT"] = 863
rid["ANDESITE"] = 862

// Igneous-Intrusive
rid["GRANITE"] = 860
rid["GABBRO"] = 859
rid["DIORITE"] = 858

// Metamorphic
rid["SLATE"] = 873
rid["SCHIST"] = 872
rid["QUARTZITE"] = 871
rid["PHYLLITE"] = 870
rid["MARBLE"] = 869
rid["GNEISS"] = 868

// Sand
rid["YELLOW-SAND"] = 910
rid["WHITE-SAND"] = 909
rid["RED-SAND"] = 908
rid["BLACK-SAND"] = 907
rid["SAND"] = 778

// Seabed
rid["CALCAREOUS-OOZE"] = 914
rid["SILICEOUS-OOZE"] = 913
rid["PELAGIC-CLAY"] = 912

// Sedimentary
rid["SILTSTONE"] = 856
rid["SHALE"] = 855
rid["SANDSTONE"] = 854
rid["ROCK-SALT"] = 853
rid["MUDSTONE"] = 852
rid["LIMESTONE"] = 851
rid["DOLOMITE"] = 850
rid["CONGLOMERATE"] = 849
rid["CLAYSTONE"] = 848
rid["CHERT"] = 847
rid["CHALK"] = 846

// Liquid
rid["MAGMA"] = 935
rid["WATER"] = 934

// Topsoil
rid["SILT-LOAM"] = 924
rid["SILTY-CLAY-LOAM"] = 923
rid["SILT"] = 922
rid["SANDY-LOAM"] = 921
rid["SANDY-CLAY-LOAM"] = 920
rid["PEAT"] = 919
rid["LOAMY-SAND"] = 918
rid["LOAM"] = 917

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
