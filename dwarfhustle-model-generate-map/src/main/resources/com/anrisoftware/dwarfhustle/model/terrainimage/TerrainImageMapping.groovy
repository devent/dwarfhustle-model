
rid = [:]

// Clay
rid["FIRE-CLAY"] = 902
rid["SILTY-CLAY"] = 900
rid["SANDY-CLAY"] = 898
rid["CLAY-LOAM"] = 896
rid["CLAY"] = 779

// Gas
rid["SULFUR-DIOXIDE"] = 930
rid["CARBON-DIOXIDE"] = 929
rid["POLLUTED-OXYGEN"] = 927
rid["OXYGEN"] = 925
rid["VACUUM"] = 924

// Igneous-Extrusive
rid["RHYOLITE"] = 864
rid["OBSIDIAN"] = 863
rid["DACITE"] = 862
rid["BASALT"] = 861
rid["ANDESITE"] = 860

// Igneous-Intrusive
rid["GRANITE"] = 858
rid["GABBRO"] = 857
rid["DIORITE"] = 856

// Metamorphic
rid["SLATE"] = 871
rid["SCHIST"] = 870
rid["QUARTZITE"] = 869
rid["PHYLLITE"] = 868
rid["MARBLE"] = 867
rid["GNEISS"] = 866

// Sand
rid["YELLOW-SAND"] = 908
rid["WHITE-SAND"] = 907
rid["RED-SAND"] = 906
rid["BLACK-SAND"] = 905
rid["SAND"] = 778

// Seabed
rid["CALCAREOUS-OOZE"] = 912
rid["SILICEOUS-OOZE"] = 911
rid["PELAGIC-CLAY"] = 910

// Sedimentary
rid["SILTSTONE"] = 854
rid["SHALE"] = 853
rid["SANDSTONE"] = 852
rid["ROCK-SALT"] = 851
rid["MUDSTONE"] = 850
rid["LIMESTONE"] = 849
rid["DOLOMITE"] = 848
rid["CONGLOMERATE"] = 847
rid["CLAYSTONE"] = 846
rid["CHERT"] = 845
rid["CHALK"] = 844

// Liquid
rid["MAGMA"] = 933
rid["WATER"] = 932

// Topsoil
rid["SILT-LOAM"] = 922
rid["SILTY-CLAY-LOAM"] = 921
rid["SILT"] = 920
rid["SANDY-LOAM"] = 919
rid["SANDY-CLAY-LOAM"] = 918
rid["PEAT"] = 917
rid["LOAMY-SAND"] = 916
rid["LOAM"] = 915

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
