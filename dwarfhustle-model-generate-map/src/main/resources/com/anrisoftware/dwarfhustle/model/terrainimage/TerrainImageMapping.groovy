
rid = [:]

// Clay
rid["FIRE-CLAY"] = 901
rid["SILTY-CLAY"] = 899
rid["SANDY-CLAY"] = 897
rid["CLAY-LOAM"] = 895
rid["CLAY"] = 779

// Gas
rid["SULFUR-DIOXIDE"] = 929
rid["CARBON-DIOXIDE"] = 928
rid["POLLUTED-OXYGEN"] = 926
rid["OXYGEN"] = 924
rid["VACUUM"] = 923

// Igneous-Extrusive
rid["RHYOLITE"] = 863
rid["OBSIDIAN"] = 862
rid["DACITE"] = 861
rid["BASALT"] = 860
rid["ANDESITE"] = 859

// Igneous-Intrusive
rid["GRANITE"] = 857
rid["GABBRO"] = 856
rid["DIORITE"] = 855

// Metamorphic
rid["SLATE"] = 870
rid["SCHIST"] = 869
rid["QUARTZITE"] = 868
rid["PHYLLITE"] = 867
rid["MARBLE"] = 866
rid["GNEISS"] = 865

// Sand
rid["YELLOW-SAND"] = 907
rid["WHITE-SAND"] = 906
rid["RED-SAND"] = 905
rid["BLACK-SAND"] = 904
rid["SAND"] = 778

// Seabed
rid["CALCAREOUS-OOZE"] = 911
rid["SILICEOUS-OOZE"] = 910
rid["PELAGIC-CLAY"] = 909

// Sedimentary
rid["SILTSTONE"] = 853
rid["SHALE"] = 852
rid["SANDSTONE"] = 851
rid["ROCK-SALT"] = 850
rid["MUDSTONE"] = 849
rid["LIMESTONE"] = 848
rid["DOLOMITE"] = 847
rid["CONGLOMERATE"] = 846
rid["CLAYSTONE"] = 845
rid["CHERT"] = 844
rid["CHALK"] = 843

// Liquid
rid["MAGMA"] = 932
rid["WATER"] = 931

// Topsoil
rid["SILT-LOAM"] = 921
rid["SILTY-CLAY-LOAM"] = 920
rid["SILT"] = 919
rid["SANDY-LOAM"] = 918
rid["SANDY-CLAY-LOAM"] = 917
rid["PEAT"] = 916
rid["LOAMY-SAND"] = 915
rid["LOAM"] = 914

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
