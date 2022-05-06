      *HEADER,COBOL,SM201A                                                            
000100 IDENTIFICATION DIVISION.                                         SM2014.2
000200 PROGRAM-ID.                                                      SM2014.2
000300     SM201A.                                                      SM2014.2
002900 DATA DIVISION.                                                   SM2014.2
008000 WORKING-STORAGE SECTION.                                         SM2014.2
014200*********************** COPY STATEMENT USED **********************SM2014.2
014300                                                                  SM2014.2
014400 01  WSTR-4.                                           COPY K1WKB SM2014.2
014500              REPLACING WSTR4A BY WSTR91                          SM2014.2
014600                        WSTR4B BY WSTR92                          SM2014.2
014700                        WSTR4C BY WSTR93.                         SM2014.2
014800                                                                  SM2014.2
014900******************** COPIED TEXT BEGINS BELOW ********************SM2014.2
016800 01  TEST-RESULTS.                                                SM2014.2
016900     02 FILLER                   PIC X      VALUE SPACE.          SM2014.2
030900 PROCEDURE DIVISION.                                              SM2014.2
000700  STOP RUN.                                                                
