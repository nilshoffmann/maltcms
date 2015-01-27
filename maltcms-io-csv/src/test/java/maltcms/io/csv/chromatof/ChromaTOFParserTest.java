/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.io.csv.chromatof;

import cross.test.LogMethodName;
import cross.test.SetupLogging;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.csv.chromatof.ChromaTOFParser.ColumnName;
import maltcms.io.csv.chromatof.ChromaTOFParser.TableColumn;
import maltcms.test.ZipResourceExtractor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class ChromaTOFParserTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    @Rule
    public SetupLogging sl = new SetupLogging();
    @Rule
    public LogMethodName lmn = new LogMethodName();

    @Test
    public void testChromaTOFParser2DRT1RT2() throws IOException {
        File dataFolder = tf.newFolder("chromaTofTestFolder");
        File file = ZipResourceExtractor.extract(
                "/csv/chromatof/reduced/2D/mut_t1_a.csv.gz", dataFolder);
        Locale locale = Locale.US;
        ChromaTOFParser parser = ChromaTOFParser.create(file, true, locale);
        LinkedHashSet<TableColumn> columnNames = parser.parseHeader(file, true, ChromaTOFParser.FIELD_SEPARATOR_COMMA, ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK);
        List<TableRow> records = parser.parseBody(columnNames, file, true, ChromaTOFParser.FIELD_SEPARATOR_COMMA, ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK);
        Assert.assertEquals(14, columnNames.size());
        Assert.assertEquals(375, records.size());
        TableRow tr2 = records.get(2);
        Assert.assertEquals(ChromaTOFParser.Mode.RT_2D_SEPARATE, parser.getMode(records));
//        "M000883_A102003-101-xxx_NA_1022.2_TRUE_VAR5_ALK_Pyridine, 3-cyano-","0-0-0","740","2.720","54340","783","945","9879","104","104","","NaN","379.21",104:4198 77:3853 50:2144 51:1881 76:1454 64:905 75:681 78:488 53:389 63:289 105:259 62:244 54:225 103:158 82:117 74:107 56:74 69:68 107:67 60:64 65:41 55:34 127:28 59:28 68:27 94:26 577:25 500:23 697:21 393:20 130:20 444:20 722:19 515:18 679:17 696:17 147:17 622:17 241:17 585:17 395:17 363:16 672:16 200:16 184:16 463:16 745:15 329:15 472:15 298:14 151:14 571:14 486:14 426:14 372:14 354:14 273:14 747:13 520:13 546:13 239:13 532:13 479:13 499:13 377:13 485:13 484:13 694:13 594:12 277:12 521:12 358:12 570:12 387:12 564:12 312:12 267:12 746:11 264:11 709:11 683:11 367:11 691:11 248:11 400:11 583:11 328:11 319:10 491:10 417:10 702:10 382:10 391:10 569:10 446:10 531:10 347:10 136:10 344:10 540:10 455:10 554:10 431:9 613:9 178:9 558:9 609:9 288:9 465:9 252:9 547:9 574:9 84:9 87:9 523:9 327:9 343:9 390:9 618:9 477:9 332:9 490:9 671:8 681:8 525:8 231:8 284:8 537:8 201:8 145:8 434:8 468:8 510:8 404:8 535:8 93:8 470:8 406:8 388:8 191:8 181:8 432:8 314:8 330:8 216:7 482:7 572:7 518:7 488:7 341:7 522:7 466:7 166:7 737:7 695:7 661:7 398:7 169:7 496:7 538:7 600:7 461:6 690:6 666:6 591:6 741:6 424:6 100:6 176:6 163:6 584:6 437:6 517:6 161:6 545:6 142:6 430:6 309:6 686:6 494:6 336:6 562:6 638:6 663:6 596:5 397:5 557:5 291:5 587:5 321:5 699:5 693:5 452:5 706:5 646:5 179:5 292:5 743:5 667:5 716:5 575:5 121:4 528:4 614:4 442:4 729:4 368:4 501:4 727:4 703:4 676:3 342:3 581:3 427:3 222:3 692:3 576:3 438:2 383:2 305:2 624:1 325:1 495:1 154:1 159:0 90:0 245:0 211:0 72:0 124:0 70:0 125:0 57:0 91:0 112:0 275:0 199:0 287:0 143:0 194:0 198:0 101:0 186:0 110:0 164:0 99:0 210:0 297:0 123:0 247:0 108:0 106:0 282:0 293:0 217:0 109:0 133:0 307:0 265:0 114:0 236:0 119:0 206:0 303:0 271:0 315:0 98:0 230:0 318:0 189:0 71:0 278:0 61:0 281:0 324:0 238:0 283:0 240:0 153:0 242:0 243:0 156:0 203:0 73:0 228:0 335:0 162:0 294:0 338:0 339:0 79:0 254:0 299:0 126:0 323:0 128:0 85:0 86:0 261:0 174:0 350:0 351:0 177:0 157:0 158:0 205:0 52:0 139:0 183:0 97:0 360:0 361:0 362:0 276:0 364:0 365:0 279:0 192:0 193:0 369:0 370:0 371:0 285:0 286:0 374:0 375:0 334:0 290:0 378:0 117:0 380:0 381:0 295:0 209:0 384:0 385:0 386:0 300:0 301:0 389:0 215:0 304:0 131:0 306:0 394:0 308:0 266:0 289:0 355:0 95:0 270:0 401:0 402:0 403:0 229:0 405:0 232:0 58:0 408:0 148:0 410:0 411:0 412:0 413:0 152:0 415:0 66:0 155:0 418:0 419:0 420:0 421:0 422:0 423:0 337:0 425:0 251:0 253:0 428:0 255:0 256:0 83:0 258:0 433:0 260:0 348:0 436:0 262:0 88:0 89:0 310:0 180:0 92:0 443:0 357:0 445:0 96:0 447:0 448:0 449:0 450:0 451:0 102:0 453:0 454:0 280:0 456:0 457:0 458:0 459:0 373:0 111:0 462:0 376:0 464:0 115:0 116:0 467:0 118:0 469:0 120:0 471:0 122:0 473:0 474:0 213:0 476:0 302:0 478:0 392:0 480:0 481:0 132:0 440:0 134:0 223:0 137:0 487:0 138:0 489:0 140:0 141:0 492:0 493:0 407:0 320:0 409:0 497:0 498:0 149:0 150:0 414:0 502:0 503:0 67:0 505:0 506:0 507:0 508:0 509:0 160:0 249:0 512:0 513:0 514:0 165:0 429:0 80:0 81:0 257:0 170:0 171:0 435:0 173:0 524:0 175:0 526:0 527:0 441:0 529:0 530:0 269:0 182:0 533:0 534:0 185:0 536:0 187:0 188:0 539:0 190:0 541:0 542:0 543:0 544:0 195:0 196:0 460:0 548:0 549:0 550:0 551:0 202:0 553:0 204:0 555:0 556:0 207:0 208:0 559:0 560:0 561:0 475:0 563:0 214:0 565:0 129:0 567:0 218:0 219:0 483:0 221:0 135:0 399:0 224:0 225:0 226:0 227:0 578:0 579:0 580:0 144:0 582:0 233:0 234:0 235:0 586:0 237:0 588:0 589:0 590:0 416:0 592:0 593:0 244:0 595:0 246:0 597:0 598:0 599:0 250:0 601:0 602:0 603:0 604:0 605:0 606:0 607:0 608:0 259:0 610:0 611:0 612:0 263:0 439:0 615:0 616:0 617:0 268:0 619:0 620:0 621:0 272:0 623:0 274:0 625:0 626:0 627:0 628:0 629:0 630:0 631:0 632:0 633:0 634:0 635:0 636:0 637:0 113:0 639:0 640:0 641:0 642:0 643:0 644:0 645:0 296:0 647:0 648:0 649:0 650:0 651:0 652:0 653:0 654:0 655:0 656:0 657:0 658:0 659:0 573:0 311:0 662:0 313:0 664:0 665:0 316:0 317:0 668:0 669:0 670:0 146:0 322:0 673:0 674:0 675:0 326:0 677:0 678:0 504:0 680:0 331:0 682:0 333:0 684:0 685:0 511:0 687:0 688:0 689:0 340:0 516:0 167:0 168:0 519:0 345:0 346:0 172:0 698:0 349:0 700:0 701:0 352:0 353:0 704:0 705:0 356:0 707:0 708:0 359:0 710:0 711:0 712:0 713:0 714:0 715:0 366:0 717:0 718:0 719:0 720:0 721:0 197:0 723:0 724:0 725:0 726:0 552:0 728:0 379:0 730:0 731:0 732:0 733:0 734:0 735:0 736:0 212:0 738:0 739:0 740:0 566:0 742:0 568:0 744:0 220:0 396:0 660:0 748:0 749:0 750:0
        Assert.assertEquals("M000883_A102003-101-xxx_NA_1022.2_TRUE_VAR5_ALK_Pyridine, 3-cyano-", tr2.getValueForName(ColumnName.NAME));
        Assert.assertEquals("0-0-0", tr2.getValueForName(ColumnName.CAS));
        Assert.assertEquals("740", tr2.getValueForName(ColumnName.FIRST_DIMENSION_TIME_SECONDS));
        Assert.assertEquals("2.720", tr2.getValueForName(ColumnName.SECOND_DIMENSION_TIME_SECONDS));
        Assert.assertEquals("104:4198 77:3853 50:2144 51:1881 76:1454 64:905 75:681 78:488 53:389 63:289 105:259 62:244 54:225 103:158 82:117 74:107 56:74 69:68 107:67 60:64 65:41 55:34 127:28 59:28 68:27 94:26 577:25 500:23 697:21 393:20 130:20 444:20 722:19 515:18 679:17 696:17 147:17 622:17 241:17 585:17 395:17 363:16 672:16 200:16 184:16 463:16 745:15 329:15 472:15 298:14 151:14 571:14 486:14 426:14 372:14 354:14 273:14 747:13 520:13 546:13 239:13 532:13 479:13 499:13 377:13 485:13 484:13 694:13 594:12 277:12 521:12 358:12 570:12 387:12 564:12 312:12 267:12 746:11 264:11 709:11 683:11 367:11 691:11 248:11 400:11 583:11 328:11 319:10 491:10 417:10 702:10 382:10 391:10 569:10 446:10 531:10 347:10 136:10 344:10 540:10 455:10 554:10 431:9 613:9 178:9 558:9 609:9 288:9 465:9 252:9 547:9 574:9 84:9 87:9 523:9 327:9 343:9 390:9 618:9 477:9 332:9 490:9 671:8 681:8 525:8 231:8 284:8 537:8 201:8 145:8 434:8 468:8 510:8 404:8 535:8 93:8 470:8 406:8 388:8 191:8 181:8 432:8 314:8 330:8 216:7 482:7 572:7 518:7 488:7 341:7 522:7 466:7 166:7 737:7 695:7 661:7 398:7 169:7 496:7 538:7 600:7 461:6 690:6 666:6 591:6 741:6 424:6 100:6 176:6 163:6 584:6 437:6 517:6 161:6 545:6 142:6 430:6 309:6 686:6 494:6 336:6 562:6 638:6 663:6 596:5 397:5 557:5 291:5 587:5 321:5 699:5 693:5 452:5 706:5 646:5 179:5 292:5 743:5 667:5 716:5 575:5 121:4 528:4 614:4 442:4 729:4 368:4 501:4 727:4 703:4 676:3 342:3 581:3 427:3 222:3 692:3 576:3 438:2 383:2 305:2 624:1 325:1 495:1 154:1 159:0 90:0 245:0 211:0 72:0 124:0 70:0 125:0 57:0 91:0 112:0 275:0 199:0 287:0 143:0 194:0 198:0 101:0 186:0 110:0 164:0 99:0 210:0 297:0 123:0 247:0 108:0 106:0 282:0 293:0 217:0 109:0 133:0 307:0 265:0 114:0 236:0 119:0 206:0 303:0 271:0 315:0 98:0 230:0 318:0 189:0 71:0 278:0 61:0 281:0 324:0 238:0 283:0 240:0 153:0 242:0 243:0 156:0 203:0 73:0 228:0 335:0 162:0 294:0 338:0 339:0 79:0 254:0 299:0 126:0 323:0 128:0 85:0 86:0 261:0 174:0 350:0 351:0 177:0 157:0 158:0 205:0 52:0 139:0 183:0 97:0 360:0 361:0 362:0 276:0 364:0 365:0 279:0 192:0 193:0 369:0 370:0 371:0 285:0 286:0 374:0 375:0 334:0 290:0 378:0 117:0 380:0 381:0 295:0 209:0 384:0 385:0 386:0 300:0 301:0 389:0 215:0 304:0 131:0 306:0 394:0 308:0 266:0 289:0 355:0 95:0 270:0 401:0 402:0 403:0 229:0 405:0 232:0 58:0 408:0 148:0 410:0 411:0 412:0 413:0 152:0 415:0 66:0 155:0 418:0 419:0 420:0 421:0 422:0 423:0 337:0 425:0 251:0 253:0 428:0 255:0 256:0 83:0 258:0 433:0 260:0 348:0 436:0 262:0 88:0 89:0 310:0 180:0 92:0 443:0 357:0 445:0 96:0 447:0 448:0 449:0 450:0 451:0 102:0 453:0 454:0 280:0 456:0 457:0 458:0 459:0 373:0 111:0 462:0 376:0 464:0 115:0 116:0 467:0 118:0 469:0 120:0 471:0 122:0 473:0 474:0 213:0 476:0 302:0 478:0 392:0 480:0 481:0 132:0 440:0 134:0 223:0 137:0 487:0 138:0 489:0 140:0 141:0 492:0 493:0 407:0 320:0 409:0 497:0 498:0 149:0 150:0 414:0 502:0 503:0 67:0 505:0 506:0 507:0 508:0 509:0 160:0 249:0 512:0 513:0 514:0 165:0 429:0 80:0 81:0 257:0 170:0 171:0 435:0 173:0 524:0 175:0 526:0 527:0 441:0 529:0 530:0 269:0 182:0 533:0 534:0 185:0 536:0 187:0 188:0 539:0 190:0 541:0 542:0 543:0 544:0 195:0 196:0 460:0 548:0 549:0 550:0 551:0 202:0 553:0 204:0 555:0 556:0 207:0 208:0 559:0 560:0 561:0 475:0 563:0 214:0 565:0 129:0 567:0 218:0 219:0 483:0 221:0 135:0 399:0 224:0 225:0 226:0 227:0 578:0 579:0 580:0 144:0 582:0 233:0 234:0 235:0 586:0 237:0 588:0 589:0 590:0 416:0 592:0 593:0 244:0 595:0 246:0 597:0 598:0 599:0 250:0 601:0 602:0 603:0 604:0 605:0 606:0 607:0 608:0 259:0 610:0 611:0 612:0 263:0 439:0 615:0 616:0 617:0 268:0 619:0 620:0 621:0 272:0 623:0 274:0 625:0 626:0 627:0 628:0 629:0 630:0 631:0 632:0 633:0 634:0 635:0 636:0 637:0 113:0 639:0 640:0 641:0 642:0 643:0 644:0 645:0 296:0 647:0 648:0 649:0 650:0 651:0 652:0 653:0 654:0 655:0 656:0 657:0 658:0 659:0 573:0 311:0 662:0 313:0 664:0 665:0 316:0 317:0 668:0 669:0 670:0 146:0 322:0 673:0 674:0 675:0 326:0 677:0 678:0 504:0 680:0 331:0 682:0 333:0 684:0 685:0 511:0 687:0 688:0 689:0 340:0 516:0 167:0 168:0 519:0 345:0 346:0 172:0 698:0 349:0 700:0 701:0 352:0 353:0 704:0 705:0 356:0 707:0 708:0 359:0 710:0 711:0 712:0 713:0 714:0 715:0 366:0 717:0 718:0 719:0 720:0 721:0 197:0 723:0 724:0 725:0 726:0 552:0 728:0 379:0 730:0 731:0 732:0 733:0 734:0 735:0 736:0 212:0 738:0 739:0 740:0 566:0 742:0 568:0 744:0 220:0 396:0 660:0 748:0 749:0 750:0", tr2.getValueForName(ColumnName.SPECTRA));

    }
    
    @Test
    public void testChromaTOFParserRt1Unmapped() throws IOException {
        File dataFolder = tf.newFolder("chromaTofTestFolder");
        File file = ZipResourceExtractor.extract(
                "/csv/chromatof/full/1D/chromatof-1D-sample-unmapped.csv.gz", dataFolder);
        Locale locale = Locale.US;
        ChromaTOFParser parser = ChromaTOFParser.create(file, true, locale);
        LinkedHashSet<TableColumn> columnNames = parser.parseHeader(file, true, ChromaTOFParser.FIELD_SEPARATOR_COMMA, ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK);
        List<TableRow> records = parser.parseBody(columnNames, file, true, ChromaTOFParser.FIELD_SEPARATOR_COMMA, ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK);
        Assert.assertEquals(17, columnNames.size());
        Assert.assertEquals(594, records.size());
        TableRow tr2 = records.get(5);
        Assert.assertEquals(ChromaTOFParser.Mode.RT_1D, parser.getMode(records));
        Assert.assertEquals("Unknown 6", tr2.getValueForName(ColumnName.NAME));
        Assert.assertEquals("664734", tr2.getValueForName(ColumnName.AREA));
        Assert.assertEquals("2.2978", tr2.getValueForName(ColumnName.FULL_WIDTH_AT_HALF_HEIGHT));
        Assert.assertEquals("73:169038 163:52151 75:19405 74:15107 164:8705 91:7165 178:7092 165:6254 90:5045 77:4206 72:2644 71:2043 76:1885 106:1598 79:1318 179:1237 133:1223 85:1010 166:924 180:883 92:870 70:751 119:648 117:563 93:521 149:488 113:403 167:308 118:220 132:215 83:210 105:188 237:177 108:155 101:141 84:135 207:114 111:112 104:94 227:87 190:86 209:84 95:83 575:81 418:80 540:71 521:70 470:69 269:68 376:67 168:67 189:66 321:66 322:65 333:63 181:63 441:63 577:63 229:63 515:62 379:62 299:60 462:60 395:59 532:59 114:58 348:58 219:57 583:57 499:57 590:57 274:57 410:57 529:57 427:55 277:55 598:55 284:55 301:54 454:54 320:53 424:53 486:53 353:52 202:51 351:50 588:49 502:48 473:47 452:47 505:47 438:46 405:46 251:46 597:45 472:45 282:45 112:45 516:44 558:44 235:44 306:44 478:44 459:44 268:43 305:43 541:43 556:43 310:42 593:41 383:41 337:41 539:41 226:41 141:40 509:40 215:40 394:40 307:39 579:39 290:39 468:39 125:39 314:38 199:38 246:38 266:38 222:38 495:37 474:37 292:37 576:37 573:37 572:37 391:37 187:36 239:36 210:36 289:36 536:36 491:35 381:35 213:35 362:35 361:35 458:35 245:35 580:35 587:35 493:34 520:34 492:34 426:34 413:33 393:33 273:33 311:33 188:33 416:33 212:32 586:32 293:32 484:32 392:32 489:31 263:31 448:31 595:31 408:31 256:31 512:31 428:31 170:30 547:30 407:30 236:30 380:29 444:29 372:29 437:29 564:29 197:28 368:28 600:28 175:28 334:28 545:27 582:27 543:27 300:27 574:27 571:27 422:27 535:26 216:26 276:26 223:26 225:26 354:26 400:25 319:25 366:25 471:24 336:24 387:24 510:23 252:23 406:23 599:23 285:23 367:23 511:23 286:22 596:22 249:22 371:22 401:22 384:21 365:21 346:21 526:21 99:21 503:21 124:21 548:20 430:20 581:20 264:20 271:20 546:20 288:19 370:19 316:19 443:18 559:18 435:18 375:18 469:18 455:17 431:17 464:17 145:17 578:17 475:17 304:17 363:16 483:16 542:16 450:16 185:16 323:16 270:15 553:15 415:15 377:15 549:14 560:14 528:14 385:14 494:14 261:14 242:14 94:13 352:13 158:13 500:12 156:11 373:10 328:10 161:10 439:9 506:9 232:7 195:7 291:7 359:7 584:6 325:6 501:4 487:4 238:3 198:3 538:1 525:1 121:0 347:0 184:0 177:0 134:0 243:0 144:0 123:0 155:0 231:0 364:0 217:0 182:0 253:0 138:0 287:0 89:0 258:0 192:0 259:0 154:0 169:0 220:0 248:0 131:0 82:0 115:0 233:0 382:0 152:0 302:0 171:0 88:0 339:0 107:0 357:0 78:0 110:0 196:0 96:0 147:0 130:0 297:0 265:0 398:0 201:0 102:0 120:0 402:0 122:0 338:0 356:0 358:0 126:0 143:0 211:0 262:0 279:0 214:0 148:0 414:0 349:0 350:0 137:0 153:0 419:0 420:0 140:0 390:0 341:0 160:0 127:0 327:0 162:0 296:0 98:0 298:0 100:0 399:0 433:0 434:0 303:0 139:0 272:0 157:0 423:0 374:0 176:0 442:0 80:0 345:0 412:0 446:0 116:0 183:0 151:0 86:0 451:0 254:0 453:0 173:0 257:0 324:0 457:0 326:0 128:0 460:0 329:0 429:0 463:0 200:0 465:0 135:0 136:0 203:0 204:0 205:0 206:0 109:0 208:0 342:0 343:0 344:0 411:0 81:0 479:0 480:0 481:0 482:0 218:0 87:0 485:0 221:0 355:0 488:0 224:0 490:0 425:0 194:0 228:0 97:0 230:0 496:0 497:0 498:0 234:0 103:0 369:0 436:0 172:0 504:0 240:0 241:0 507:0 508:0 476:0 477:0 445:0 247:0 513:0 514:0 250:0 186:0 517:0 386:0 519:0 388:0 389:0 522:0 523:0 524:0 260:0 129:0 527:0 396:0 397:0 530:0 531:0 267:0 533:0 534:0 403:0 404:0 537:0 340:0 142:0 275:0 409:0 146:0 278:0 544:0 280:0 281:0 150:0 283:0 417:0 550:0 551:0 552:0 421:0 554:0 555:0 159:0 557:0 360:0 294:0 295:0 561:0 562:0 563:0 432:0 565:0 566:0 567:0 568:0 569:0 570:0 174:0 440:0 308:0 309:0 244:0 378:0 312:0 313:0 447:0 315:0 449:0 317:0 318:0 518:0 585:0 255:0 191:0 456:0 589:0 193:0 591:0 592:0 461:0 594:0 330:0 331:0 332:0 466:0 467:0 335:0", tr2.getValueForName(ColumnName.SPECTRA));
    }
    
    @Test
    public void testChromaTOFParser2DRT() throws IOException {
        File dataFolder = tf.newFolder("chromaTofTestFolder");
        File file = ZipResourceExtractor.extract(
                "/csv/chromatof/full/2D/mut_t1_a.csv.gz", dataFolder);
        Locale locale = Locale.US;
        ChromaTOFParser parser = ChromaTOFParser.create(file, true, locale);
        LinkedHashSet<TableColumn> columnNames = parser.parseHeader(file, true, ChromaTOFParser.FIELD_SEPARATOR_COMMA, ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK);
        List<TableRow> records = parser.parseBody(columnNames, file, true, ChromaTOFParser.FIELD_SEPARATOR_COMMA, ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK);
        Assert.assertEquals(26, columnNames.size());
        Assert.assertEquals(2451, records.size());
        Assert.assertEquals(ChromaTOFParser.Mode.RT_2D_FUSED, parser.getMode(records));
        TableRow tr2 = records.get(2);
//"Unknown 3","680 , 2.520","Unknown","111","","","","111","196.63","55576","","Uncalibrated","0.10282","680 , 2.300,333","680 , 2.740,327","M001165_A178020-101-xxx_NA_1781.11_TRUE_VAR5_ALK_Glutamine, N-gamma-ethyl- (2TMS)","334","425","5283","0-00-0","GMD_20100614_VAR5_ALK_MSP","712","C13H30N2O3Si2","318","consensus spectrum of 1 spectra per analyte, MPIMP ID and isotopomer, with majority threshold = 60%",54:5124 111:2036 83:1456 53:1399 82:1340 56:1059 55:726 57:311 66:253 110:246 67:169 91:90 112:86 144:39 98:24 106:23 107:22 92:19 465:15 540:11 665:11 646:11 279:9 651:9 731:9 750:8 109:8 462:8 284:7 523:6 552:6 529:6 629:5 442:5 304:5 351:4 425:4 636:4 343:4 333:4 634:4 553:3 59:0 79:0 85:0 65:0 52:0 74:0 96:0 99:0 77:0 58:0 80:0 51:0 61:0 84:0 63:0 64:0 87:0 88:0 100:0 90:0 102:0 60:0 114:0 94:0 95:0 117:0 118:0 76:0 120:0 78:0 122:0 123:0 124:0 125:0 62:0 127:0 128:0 108:0 130:0 131:0 132:0 133:0 134:0 135:0 72:0 137:0 138:0 139:0 97:0 141:0 142:0 143:0 101:0 145:0 146:0 147:0 148:0 149:0 150:0 151:0 152:0 153:0 68:0 69:0 70:0 157:0 71:0 73:0 160:0 161:0 162:0 163:0 164:0 165:0 166:0 167:0 81:0 169:0 170:0 171:0 172:0 86:0 174:0 175:0 89:0 177:0 178:0 136:0 93:0 181:0 182:0 183:0 184:0 185:0 186:0 187:0 188:0 189:0 103:0 191:0 105:0 193:0 194:0 195:0 196:0 154:0 155:0 156:0 113:0 201:0 159:0 116:0 204:0 205:0 119:0 207:0 208:0 209:0 210:0 211:0 212:0 213:0 214:0 215:0 216:0 217:0 218:0 219:0 220:0 221:0 222:0 223:0 224:0 225:0 226:0 227:0 228:0 229:0 230:0 231:0 232:0 233:0 234:0 235:0 236:0 237:0 238:0 239:0 240:0 241:0 242:0 243:0 244:0 245:0 246:0 247:0 248:0 249:0 250:0 251:0 252:0 253:0 254:0 255:0 256:0 257:0 258:0 259:0 260:0 261:0 262:0 263:0 264:0 265:0 266:0 180:0 268:0 269:0 270:0 271:0 272:0 273:0 274:0 275:0 276:0 277:0 278:0 192:0 280:0 281:0 282:0 283:0 197:0 198:0 199:0 200:0 288:0 289:0 290:0 291:0 292:0 293:0 294:0 295:0 121:0 297:0 298:0 299:0 300:0 126:0 302:0 303:0 129:0 305:0 306:0 307:0 308:0 309:0 310:0 311:0 312:0 313:0 314:0 140:0 316:0 317:0 318:0 319:0 320:0 321:0 322:0 323:0 324:0 325:0 326:0 327:0 328:0 329:0 330:0 331:0 332:0 158:0 334:0 335:0 336:0 337:0 338:0 339:0 340:0 341:0 342:0 168:0 344:0 345:0 346:0 347:0 348:0 349:0 350:0 176:0 352:0 353:0 354:0 355:0 356:0 357:0 358:0 359:0 360:0 361:0 362:0 363:0 364:0 365:0 366:0 367:0 368:0 369:0 370:0 371:0 285:0 373:0 374:0 375:0 376:0 377:0 378:0 379:0 380:0 206:0 382:0 383:0 384:0 385:0 386:0 387:0 388:0 389:0 390:0 391:0 392:0 393:0 394:0 395:0 396:0 397:0 398:0 399:0 50:0 401:0 402:0 403:0 404:0 405:0 406:0 407:0 408:0 409:0 410:0 411:0 412:0 413:0 414:0 415:0 416:0 417:0 418:0 419:0 420:0 421:0 422:0 423:0 424:0 75:0 426:0 427:0 428:0 429:0 430:0 431:0 432:0 433:0 434:0 435:0 436:0 437:0 438:0 439:0 440:0 441:0 267:0 443:0 444:0 445:0 446:0 447:0 448:0 449:0 450:0 451:0 452:0 453:0 104:0 455:0 456:0 457:0 458:0 372:0 460:0 461:0 287:0 463:0 464:0 115:0 466:0 467:0 468:0 469:0 470:0 471:0 472:0 473:0 474:0 475:0 476:0 477:0 478:0 479:0 480:0 481:0 482:0 483:0 484:0 485:0 486:0 487:0 488:0 489:0 490:0 491:0 492:0 493:0 494:0 495:0 496:0 497:0 498:0 499:0 500:0 501:0 502:0 503:0 504:0 505:0 506:0 507:0 508:0 509:0 510:0 511:0 512:0 513:0 514:0 515:0 516:0 517:0 518:0 519:0 520:0 521:0 522:0 173:0 524:0 525:0 526:0 527:0 528:0 179:0 530:0 531:0 532:0 533:0 534:0 535:0 536:0 537:0 538:0 539:0 190:0 541:0 542:0 543:0 544:0 545:0 546:0 547:0 548:0 549:0 550:0 551:0 202:0 203:0 554:0 555:0 556:0 557:0 558:0 559:0 560:0 561:0 562:0 563:0 564:0 565:0 566:0 567:0 568:0 569:0 570:0 571:0 572:0 573:0 574:0 575:0 576:0 577:0 578:0 579:0 580:0 581:0 582:0 583:0 584:0 585:0 586:0 587:0 588:0 589:0 590:0 591:0 592:0 593:0 594:0 595:0 596:0 597:0 598:0 599:0 600:0 601:0 602:0 603:0 604:0 605:0 606:0 607:0 608:0 609:0 610:0 611:0 612:0 613:0 614:0 615:0 616:0 617:0 618:0 619:0 620:0 621:0 622:0 623:0 624:0 625:0 626:0 627:0 628:0 454:0 630:0 631:0 632:0 633:0 459:0 635:0 286:0 637:0 638:0 639:0 640:0 641:0 642:0 643:0 644:0 645:0 296:0 647:0 648:0 649:0 650:0 301:0 652:0 653:0 654:0 655:0 656:0 657:0 658:0 659:0 660:0 661:0 662:0 663:0 664:0 315:0 666:0 667:0 668:0 669:0 670:0 671:0 672:0 673:0 674:0 675:0 676:0 677:0 678:0 679:0 680:0 681:0 682:0 683:0 684:0 685:0 686:0 687:0 688:0 689:0 690:0 691:0 692:0 693:0 694:0 695:0 696:0 697:0 698:0 699:0 700:0 701:0 702:0 703:0 704:0 705:0 706:0 707:0 708:0 709:0 710:0 711:0 712:0 713:0 714:0 715:0 716:0 717:0 718:0 719:0 720:0 721:0 722:0 723:0 724:0 725:0 726:0 727:0 728:0 729:0 730:0 381:0 732:0 733:0 734:0 735:0 736:0 737:0 738:0 739:0 740:0 741:0 742:0 743:0 744:0 745:0 746:0 747:0 748:0 749:0 400:0
        Assert.assertEquals("Unknown 3", tr2.getValueForName(ColumnName.NAME));
        Assert.assertEquals("680 , 2.520", tr2.getValueForName(ColumnName.RETENTION_TIME_SECONDS));
        Assert.assertEquals("54:5124 111:2036 83:1456 53:1399 82:1340 56:1059 55:726 57:311 66:253 110:246 67:169 91:90 112:86 144:39 98:24 106:23 107:22 92:19 465:15 540:11 665:11 646:11 279:9 651:9 731:9 750:8 109:8 462:8 284:7 523:6 552:6 529:6 629:5 442:5 304:5 351:4 425:4 636:4 343:4 333:4 634:4 553:3 59:0 79:0 85:0 65:0 52:0 74:0 96:0 99:0 77:0 58:0 80:0 51:0 61:0 84:0 63:0 64:0 87:0 88:0 100:0 90:0 102:0 60:0 114:0 94:0 95:0 117:0 118:0 76:0 120:0 78:0 122:0 123:0 124:0 125:0 62:0 127:0 128:0 108:0 130:0 131:0 132:0 133:0 134:0 135:0 72:0 137:0 138:0 139:0 97:0 141:0 142:0 143:0 101:0 145:0 146:0 147:0 148:0 149:0 150:0 151:0 152:0 153:0 68:0 69:0 70:0 157:0 71:0 73:0 160:0 161:0 162:0 163:0 164:0 165:0 166:0 167:0 81:0 169:0 170:0 171:0 172:0 86:0 174:0 175:0 89:0 177:0 178:0 136:0 93:0 181:0 182:0 183:0 184:0 185:0 186:0 187:0 188:0 189:0 103:0 191:0 105:0 193:0 194:0 195:0 196:0 154:0 155:0 156:0 113:0 201:0 159:0 116:0 204:0 205:0 119:0 207:0 208:0 209:0 210:0 211:0 212:0 213:0 214:0 215:0 216:0 217:0 218:0 219:0 220:0 221:0 222:0 223:0 224:0 225:0 226:0 227:0 228:0 229:0 230:0 231:0 232:0 233:0 234:0 235:0 236:0 237:0 238:0 239:0 240:0 241:0 242:0 243:0 244:0 245:0 246:0 247:0 248:0 249:0 250:0 251:0 252:0 253:0 254:0 255:0 256:0 257:0 258:0 259:0 260:0 261:0 262:0 263:0 264:0 265:0 266:0 180:0 268:0 269:0 270:0 271:0 272:0 273:0 274:0 275:0 276:0 277:0 278:0 192:0 280:0 281:0 282:0 283:0 197:0 198:0 199:0 200:0 288:0 289:0 290:0 291:0 292:0 293:0 294:0 295:0 121:0 297:0 298:0 299:0 300:0 126:0 302:0 303:0 129:0 305:0 306:0 307:0 308:0 309:0 310:0 311:0 312:0 313:0 314:0 140:0 316:0 317:0 318:0 319:0 320:0 321:0 322:0 323:0 324:0 325:0 326:0 327:0 328:0 329:0 330:0 331:0 332:0 158:0 334:0 335:0 336:0 337:0 338:0 339:0 340:0 341:0 342:0 168:0 344:0 345:0 346:0 347:0 348:0 349:0 350:0 176:0 352:0 353:0 354:0 355:0 356:0 357:0 358:0 359:0 360:0 361:0 362:0 363:0 364:0 365:0 366:0 367:0 368:0 369:0 370:0 371:0 285:0 373:0 374:0 375:0 376:0 377:0 378:0 379:0 380:0 206:0 382:0 383:0 384:0 385:0 386:0 387:0 388:0 389:0 390:0 391:0 392:0 393:0 394:0 395:0 396:0 397:0 398:0 399:0 50:0 401:0 402:0 403:0 404:0 405:0 406:0 407:0 408:0 409:0 410:0 411:0 412:0 413:0 414:0 415:0 416:0 417:0 418:0 419:0 420:0 421:0 422:0 423:0 424:0 75:0 426:0 427:0 428:0 429:0 430:0 431:0 432:0 433:0 434:0 435:0 436:0 437:0 438:0 439:0 440:0 441:0 267:0 443:0 444:0 445:0 446:0 447:0 448:0 449:0 450:0 451:0 452:0 453:0 104:0 455:0 456:0 457:0 458:0 372:0 460:0 461:0 287:0 463:0 464:0 115:0 466:0 467:0 468:0 469:0 470:0 471:0 472:0 473:0 474:0 475:0 476:0 477:0 478:0 479:0 480:0 481:0 482:0 483:0 484:0 485:0 486:0 487:0 488:0 489:0 490:0 491:0 492:0 493:0 494:0 495:0 496:0 497:0 498:0 499:0 500:0 501:0 502:0 503:0 504:0 505:0 506:0 507:0 508:0 509:0 510:0 511:0 512:0 513:0 514:0 515:0 516:0 517:0 518:0 519:0 520:0 521:0 522:0 173:0 524:0 525:0 526:0 527:0 528:0 179:0 530:0 531:0 532:0 533:0 534:0 535:0 536:0 537:0 538:0 539:0 190:0 541:0 542:0 543:0 544:0 545:0 546:0 547:0 548:0 549:0 550:0 551:0 202:0 203:0 554:0 555:0 556:0 557:0 558:0 559:0 560:0 561:0 562:0 563:0 564:0 565:0 566:0 567:0 568:0 569:0 570:0 571:0 572:0 573:0 574:0 575:0 576:0 577:0 578:0 579:0 580:0 581:0 582:0 583:0 584:0 585:0 586:0 587:0 588:0 589:0 590:0 591:0 592:0 593:0 594:0 595:0 596:0 597:0 598:0 599:0 600:0 601:0 602:0 603:0 604:0 605:0 606:0 607:0 608:0 609:0 610:0 611:0 612:0 613:0 614:0 615:0 616:0 617:0 618:0 619:0 620:0 621:0 622:0 623:0 624:0 625:0 626:0 627:0 628:0 454:0 630:0 631:0 632:0 633:0 459:0 635:0 286:0 637:0 638:0 639:0 640:0 641:0 642:0 643:0 644:0 645:0 296:0 647:0 648:0 649:0 650:0 301:0 652:0 653:0 654:0 655:0 656:0 657:0 658:0 659:0 660:0 661:0 662:0 663:0 664:0 315:0 666:0 667:0 668:0 669:0 670:0 671:0 672:0 673:0 674:0 675:0 676:0 677:0 678:0 679:0 680:0 681:0 682:0 683:0 684:0 685:0 686:0 687:0 688:0 689:0 690:0 691:0 692:0 693:0 694:0 695:0 696:0 697:0 698:0 699:0 700:0 701:0 702:0 703:0 704:0 705:0 706:0 707:0 708:0 709:0 710:0 711:0 712:0 713:0 714:0 715:0 716:0 717:0 718:0 719:0 720:0 721:0 722:0 723:0 724:0 725:0 726:0 727:0 728:0 729:0 730:0 381:0 732:0 733:0 734:0 735:0 736:0 737:0 738:0 739:0 740:0 741:0 742:0 743:0 744:0 745:0 746:0 747:0 748:0 749:0 400:0", tr2.getValueForName(ColumnName.SPECTRA));
    }
}
