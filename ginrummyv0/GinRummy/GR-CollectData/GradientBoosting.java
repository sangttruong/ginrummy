public class GradientBoosting {

    public static double[] predict(double[] input) {
        double var0;
        if ((input[1]) >= (1.5)) {
            if ((input[1]) >= (2.5)) {
                var0 = 0.198545456;
            } else {
                if ((input[3]) >= (0.5)) {
                    if ((input[3]) >= (1.5)) {
                        var0 = 0.0218579229;
                    } else {
                        var0 = 0.0294287354;
                    }
                } else {
                    var0 = 0.0506769828;
                }
            }
        } else {
            if ((input[1]) >= (0.5)) {
                if ((input[3]) >= (0.5)) {
                    if ((input[3]) >= (2.5)) {
                        if ((input[3]) >= (3.5)) {
                            var0 = 0.0666666701;
                        } else {
                            var0 = 0.0246153846;
                        }
                    } else {
                        if ((input[3]) >= (1.5)) {
                            var0 = 0.00246549374;
                        } else {
                            var0 = 0.00378714874;
                        }
                    }
                } else {
                    var0 = 0.0110377939;
                }
            } else {
                if ((input[3]) >= (0.5)) {
                    if ((input[3]) >= (1.5)) {
                        if ((input[3]) >= (3.5)) {
                            var0 = -0.0385150835;
                        } else {
                            var0 = -0.0214866605;
                        }
                    } else {
                        var0 = -0.0134930899;
                    }
                } else {
                    var0 = -0.00335710007;
                }
            }
        }
        double var1;
        if ((input[6]) >= (17.5)) {
            if ((input[6]) >= (32.5)) {
                if ((input[6]) >= (52.5)) {
                    if ((input[6]) >= (62.5)) {
                        if ((input[6]) >= (94.5)) {
                            var1 = 0.0238385275;
                        } else {
                            var1 = -0.032003697;
                        }
                    } else {
                        if ((input[6]) >= (54.5)) {
                            var1 = -0.0251468867;
                        } else {
                            var1 = -0.0206153784;
                        }
                    }
                } else {
                    if ((input[6]) >= (42.5)) {
                        if ((input[6]) >= (50.5)) {
                            var1 = -0.0174134541;
                        } else {
                            var1 = -0.0149012078;
                        }
                    } else {
                        if ((input[6]) >= (40.5)) {
                            var1 = -0.00931743439;
                        } else {
                            var1 = -0.00531066535;
                        }
                    }
                }
            } else {
                if ((input[6]) >= (25.5)) {
                    if ((input[6]) >= (29.5)) {
                        if ((input[6]) >= (30.5)) {
                            var1 = 0.00151900644;
                        } else {
                            var1 = -0.00048967544;
                        }
                    } else {
                        if ((input[6]) >= (28.5)) {
                            var1 = 0.00929948501;
                        } else {
                            var1 = 0.00577958068;
                        }
                    }
                } else {
                    if ((input[6]) >= (19.5)) {
                        if ((input[6]) >= (22.5)) {
                            var1 = 0.015214934;
                        } else {
                            var1 = 0.0210176017;
                        }
                    } else {
                        if ((input[6]) >= (18.5)) {
                            var1 = 0.0330042951;
                        } else {
                            var1 = 0.0372779928;
                        }
                    }
                }
            }
        } else {
            if ((input[6]) >= (10.5)) {
                if ((input[6]) >= (15.5)) {
                    if ((input[6]) >= (16.5)) {
                        var1 = 0.0438598692;
                    } else {
                        var1 = 0.0561867654;
                    }
                } else {
                    if ((input[6]) >= (11.5)) {
                        if ((input[6]) >= (12.5)) {
                            var1 = 0.0697816163;
                        } else {
                            var1 = 0.075076066;
                        }
                    } else {
                        var1 = 0.083939001;
                    }
                }
            } else {
                var1 = 0.197337136;
            }
        }
        double var2;
        if ((input[0]) >= (0.5)) {
            if ((input[1]) >= (0.5)) {
                if ((input[1]) >= (1.5)) {
                    var2 = 0.179054424;
                } else {
                    if ((input[0]) >= (1.5)) {
                        var2 = 0.181643933;
                    } else {
                        var2 = 0.0651802942;
                    }
                }
            } else {
                if ((input[0]) >= (1.5)) {
                    if ((input[0]) >= (2.5)) {
                        var2 = 0.179725826;
                    } else {
                        var2 = 0.0858896598;
                    }
                } else {
                    var2 = 0.00835885294;
                }
            }
        } else {
            if ((input[1]) >= (1.5)) {
                if ((input[1]) >= (2.5)) {
                    var2 = 0.167863816;
                } else {
                    var2 = 0.0339471474;
                }
            } else {
                if ((input[1]) >= (0.5)) {
                    var2 = -0.0137812495;
                } else {
                    var2 = -0.0375820994;
                }
            }
        }
        double var3;
        if ((input[0]) >= (0.5)) {
            if ((input[1]) >= (0.5)) {
                if ((input[1]) >= (1.5)) {
                    var3 = 0.165976301;
                } else {
                    if ((input[0]) >= (1.5)) {
                        var3 = 0.16794017;
                    } else {
                        var3 = 0.0588298105;
                    }
                }
            } else {
                if ((input[0]) >= (1.5)) {
                    if ((input[0]) >= (2.5)) {
                        var3 = 0.166152343;
                    } else {
                        var3 = 0.077588141;
                    }
                } else {
                    var3 = 0.0075227879;
                }
            }
        } else {
            if ((input[1]) >= (1.5)) {
                if ((input[1]) >= (2.5)) {
                    var3 = 0.157227919;
                } else {
                    var3 = 0.0306093823;
                }
            } else {
                if ((input[1]) >= (0.5)) {
                    var3 = -0.0124034146;
                } else {
                    var3 = -0.0338609554;
                }
            }
        }
        double var4;
        if ((input[6]) >= (16.5)) {
            if ((input[0]) >= (0.5)) {
                if ((input[0]) >= (1.5)) {
                    if ((input[6]) >= (19.5)) {
                        if ((input[6]) >= (29.5)) {
                            var4 = 0.0368024968;
                        } else {
                            var4 = 0.0493138209;
                        }
                    } else {
                        if ((input[6]) >= (17.5)) {
                            var4 = 0.0673150122;
                        } else {
                            var4 = 0.0780134127;
                        }
                    }
                } else {
                    if ((input[6]) >= (24.5)) {
                        if ((input[6]) >= (31.5)) {
                            var4 = 0.00662321597;
                        } else {
                            var4 = 0.0137876635;
                        }
                    } else {
                        if ((input[6]) >= (19.5)) {
                            var4 = 0.0218569841;
                        } else {
                            var4 = 0.0331146605;
                        }
                    }
                }
            } else {
                if ((input[6]) >= (44.5)) {
                    if ((input[6]) >= (54.5)) {
                        if ((input[6]) >= (57.5)) {
                            var4 = -0.0218855273;
                        } else {
                            var4 = -0.0265469346;
                        }
                    } else {
                        if ((input[6]) >= (51.5)) {
                            var4 = -0.0196277183;
                        } else {
                            var4 = -0.0208834987;
                        }
                    }
                } else {
                    if ((input[6]) >= (19.5)) {
                        if ((input[6]) >= (32.5)) {
                            var4 = -0.0152865006;
                        } else {
                            var4 = -0.0122393379;
                        }
                    } else {
                        if ((input[6]) >= (17.5)) {
                            var4 = -0.00455122953;
                        } else {
                            var4 = 0.00699709356;
                        }
                    }
                }
            }
        } else {
            if ((input[6]) >= (10.5)) {
                if ((input[0]) >= (0.5)) {
                    if ((input[0]) >= (1.5)) {
                        if ((input[6]) >= (12.5)) {
                            var4 = 0.0799326822;
                        } else {
                            var4 = 0.0926319361;
                        }
                    } else {
                        if ((input[6]) >= (15.5)) {
                            var4 = 0.0493512563;
                        } else {
                            var4 = 0.0629697666;
                        }
                    }
                } else {
                    if ((input[6]) >= (11.5)) {
                        if ((input[6]) >= (15.5)) {
                            var4 = 0.0151434094;
                        } else {
                            var4 = 0.0263575613;
                        }
                    } else {
                        var4 = 0.0478638299;
                    }
                }
            } else {
                if ((input[6]) >= (5.5)) {
                    var4 = 0.170283213;
                } else {
                    var4 = 0.157940775;
                }
            }
        }
        double var5;
        if ((input[6]) >= (10.5)) {
            if ((input[6]) >= (19.5)) {
                if ((input[6]) >= (40.5)) {
                    if ((input[6]) >= (54.5)) {
                        if ((input[6]) >= (62.5)) {
                            var5 = -0.0198908057;
                        } else {
                            var5 = -0.0153743299;
                        }
                    } else {
                        if ((input[6]) >= (42.5)) {
                            var5 = -0.0107210251;
                        } else {
                            var5 = -0.00662960485;
                        }
                    }
                } else {
                    if ((input[6]) >= (25.5)) {
                        if ((input[6]) >= (32.5)) {
                            var5 = -0.00413776468;
                        } else {
                            var5 = 0.00127221062;
                        }
                    } else {
                        if ((input[6]) >= (22.5)) {
                            var5 = 0.00829800498;
                        } else {
                            var5 = 0.0116791977;
                        }
                    }
                }
            } else {
                if ((input[6]) >= (16.5)) {
                    if ((input[6]) >= (17.5)) {
                        if ((input[6]) >= (18.5)) {
                            var5 = 0.0201701559;
                        } else {
                            var5 = 0.023256544;
                        }
                    } else {
                        var5 = 0.0279329326;
                    }
                } else {
                    if ((input[6]) >= (15.5)) {
                        var5 = 0.0377633385;
                    } else {
                        if ((input[6]) >= (11.5)) {
                            var5 = 0.0491037108;
                        } else {
                            var5 = 0.0592916012;
                        }
                    }
                }
            }
        } else {
            var5 = 0.158205733;
        }
        double var6;
        if ((input[6]) >= (10.5)) {
            if ((input[6]) >= (19.5)) {
                if ((input[6]) >= (40.5)) {
                    if ((input[6]) >= (54.5)) {
                        if ((input[3]) >= (0.5)) {
                            var6 = -0.0141779156;
                        } else {
                            var6 = -0.019590361;
                        }
                    } else {
                        if ((input[3]) >= (1.5)) {
                            var6 = 0.000404231629;
                        } else {
                            var6 = -0.01024001;
                        }
                    }
                } else {
                    if ((input[6]) >= (25.5)) {
                        if ((input[6]) >= (31.5)) {
                            var6 = -0.00342926383;
                        } else {
                            var6 = 0.00155273301;
                        }
                    } else {
                        if ((input[3]) >= (0.5)) {
                            var6 = 0.00406459859;
                        } else {
                            var6 = 0.0109831532;
                        }
                    }
                }
            } else {
                if ((input[6]) >= (16.5)) {
                    if ((input[6]) >= (17.5)) {
                        if ((input[3]) >= (0.5)) {
                            var6 = 0.0111583658;
                        } else {
                            var6 = 0.0228398982;
                        }
                    } else {
                        if ((input[3]) >= (0.5)) {
                            var6 = 0.0314289294;
                        } else {
                            var6 = 0.0232033134;
                        }
                    }
                } else {
                    if ((input[6]) >= (15.5)) {
                        if ((input[3]) >= (0.5)) {
                            var6 = 0.0284346174;
                        } else {
                            var6 = 0.0362012051;
                        }
                    } else {
                        if ((input[6]) >= (11.5)) {
                            var6 = 0.0444994532;
                        } else {
                            var6 = 0.0538841486;
                        }
                    }
                }
            }
        } else {
            var6 = 0.149716899;
        }
        double var7;
        if ((input[6]) >= (10.5)) {
            if ((input[6]) >= (19.5)) {
                if ((input[5]) >= (0.5)) {
                    if ((input[6]) >= (40.5)) {
                        if ((input[5]) >= (1.5)) {
                            var7 = -0.0207015481;
                        } else {
                            var7 = -0.0125409635;
                        }
                    } else {
                        if ((input[5]) >= (2.5)) {
                            var7 = -0.0338797383;
                        } else {
                            var7 = -0.00568651641;
                        }
                    }
                } else {
                    if ((input[6]) >= (29.5)) {
                        if ((input[6]) >= (60.5)) {
                            var7 = -0.0174975079;
                        } else {
                            var7 = -7.72127896e-05;
                        }
                    } else {
                        if ((input[6]) >= (24.5)) {
                            var7 = 0.0100815101;
                        } else {
                            var7 = 0.017413443;
                        }
                    }
                }
            } else {
                if ((input[5]) >= (0.5)) {
                    if ((input[6]) >= (15.5)) {
                        if ((input[5]) >= (1.5)) {
                            var7 = -0.0196905546;
                        } else {
                            var7 = 0.00436997926;
                        }
                    } else {
                        if ((input[5]) >= (1.5)) {
                            var7 = 0.000879604369;
                        } else {
                            var7 = 0.04262105;
                        }
                    }
                } else {
                    if ((input[6]) >= (16.5)) {
                        if ((input[6]) >= (17.5)) {
                            var7 = 0.0259719472;
                        } else {
                            var7 = 0.0294466764;
                        }
                    } else {
                        if ((input[6]) >= (12.5)) {
                            var7 = 0.0404071175;
                        } else {
                            var7 = 0.0466343388;
                        }
                    }
                }
            }
        } else {
            var7 = 0.142821833;
        }
        double var8;
        if ((input[6]) >= (10.5)) {
            if ((input[6]) >= (19.5)) {
                if ((input[6]) >= (42.5)) {
                    if ((input[6]) >= (62.5)) {
                        if ((input[6]) >= (67.5)) {
                            var8 = -0.0136558656;
                        } else {
                            var8 = -0.0165201724;
                        }
                    } else {
                        if ((input[6]) >= (52.5)) {
                            var8 = -0.0109386379;
                        } else {
                            var8 = -0.00751552684;
                        }
                    }
                } else {
                    if ((input[6]) >= (29.5)) {
                        if ((input[6]) >= (32.5)) {
                            var8 = -0.00329755433;
                        } else {
                            var8 = -0.000372870854;
                        }
                    } else {
                        if ((input[6]) >= (25.5)) {
                            var8 = 0.00209995057;
                        } else {
                            var8 = 0.00723351026;
                        }
                    }
                }
            } else {
                if ((input[6]) >= (16.5)) {
                    if ((input[6]) >= (17.5)) {
                        if ((input[6]) >= (18.5)) {
                            var8 = 0.0145413233;
                        } else {
                            var8 = 0.017238168;
                        }
                    } else {
                        var8 = 0.0206095073;
                    }
                } else {
                    if ((input[6]) >= (15.5)) {
                        var8 = 0.0278934427;
                    } else {
                        if ((input[6]) >= (11.5)) {
                            var8 = 0.0365552865;
                        } else {
                            var8 = 0.0448762588;
                        }
                    }
                }
            }
        } else {
            var8 = 0.137132868;
        }
        double var9;
        if ((input[0]) >= (0.5)) {
            if ((input[0]) >= (1.5)) {
                if ((input[0]) >= (2.5)) {
                    var9 = 0.127412543;
                } else {
                    if ((input[2]) >= (1.5)) {
                        var9 = 0.0683662444;
                    } else {
                        var9 = 0.0553980768;
                    }
                }
            } else {
                if ((input[2]) >= (0.5)) {
                    if ((input[2]) >= (1.5)) {
                        if ((input[2]) >= (2.5)) {
                            var9 = 0.0128866481;
                        } else {
                            var9 = 0.00704110414;
                        }
                    } else {
                        var9 = 0.0143943382;
                    }
                } else {
                    var9 = 0.0296403449;
                }
            }
        } else {
            if ((input[2]) >= (1.5)) {
                if ((input[2]) >= (2.5)) {
                    if ((input[2]) >= (3.5)) {
                        if ((input[2]) >= (4.5)) {
                            var9 = -0.0191070996;
                        } else {
                            var9 = -0.0236221924;
                        }
                    } else {
                        var9 = -0.0212529842;
                    }
                } else {
                    var9 = -0.0130818831;
                }
            } else {
                if ((input[2]) >= (0.5)) {
                    var9 = -0.00573914079;
                } else {
                    var9 = 0.00428555673;
                }
            }
        }
        double var10;
        var10 = (1) / ((1) + (Math.exp((0) - (((((((((((-0.0) + (var0)) + (var1)) + (var2)) + (var3)) + (var4)) + (var5)) + (var6)) + (var7)) + (var8)) + (var9)))));
        return new double[] {(1) - (var10), var10};
    }
}
