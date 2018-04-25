package com.cm55.kanhira.dict;

import com.cm55.kanhira.converter.*;

/*
 * $Id: KanjiYomi.java,v 1.5 2003/01/01 08:18:44 kawao Exp $
 *
 * KAKASI/JAVA
 *  Copyright (C) 2002-2003  KAWAO, Tomoyuki (kawao@kawao.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

/**
 * An object of this class holds a yomi of a kanji.
 * 
 * @author Kawao, Tomoyuki (kawao@kawao.com)
 * @version $Revision: 1.5 $ $Date: 2003/01/01 08:18:44 $
 */
public class KanjiYomi implements Comparable<KanjiYomi> {

  private static final Object LOCK = new Object();
  private static long objectConter;
  private final long objectIndex;

  /** 
   * 漢字、最初の文字を除いた部分、「悪代官」の場合は「代官」が格納される。
   * 「悪巧み」の場合は「巧み」、この場合の「み」は送り仮名ではなく漢字の一部。
   * また、「貴ノ花」の場合は「ノ花」だが、このようにカタカナがまじる場合もある。 */
  private final String kanji;
  
  /** よみ、上の例の場合、最初の文字分を含む「あくだいかん」が格納される。 */
  private final String yomi;
  
  /** 
   * 送り仮名が無い場合は(char)0、ある場合はそのイニシャルの半角アルファベット小文字。
   * 例えば「悪い」の場合は「i」が格納される   */
  private final char okuriIni;
  
  /** 
   * 漢字部分の長さ、「悪代官」の場合は「代官」で2、「悪い」の場合は送り仮名を含まずに0。
   * 「悪巧み」の場合は「巧み」で2、この場合の「み」は送り仮名ではない。 */
  private final int kanjiLength;
  
  /** ハッシュ値 */
  private final int hashCode;

  /**
   * Constructs a KanjiYomi object.
   * 
   * @param kanji 最初の文字を除いた漢字部分。「悪名高い」の場合は、送り仮名も除いて「名高」になる。
   * 実際には送り仮名で無い「かな」や「カナ」も含まれる。例えば、「悪巧み」の「み」は送り仮名ではない。
   * 「貴ノ花」には「カナ」が含まれる。
   * @param yomi よみ。上の場合は「あくめいたか」になる。
   * @param okurigana　送り仮名がなければ(char)0、あれば上の場合は'i'になる。
   */
  public KanjiYomi(String kanji, String yomi, char okuriIni) {
    
    // kanjiは、実際にはどのような文字種でもよい。
    
    if (!CharKind.isHiragana(yomi)) 
      throw new IllegalArgumentException("illegal yomi " + yomi);
    if (okuriIni != 0) {
      if (!CharKind.isLowerAlphabet((char)okuriIni))
        throw new IllegalArgumentException("illgal okuriIni");
    }
        
    this.kanji = kanji;
    this.yomi = yomi;
    this.okuriIni = okuriIni;
    kanjiLength = kanji.length();
    hashCode = kanji.hashCode() ^ yomi.hashCode() ^ (int) okuriIni;
    
    // このオブジェクトの生成順を保持するらしい
    synchronized (LOCK) {
      objectIndex = objectConter++;
    }
  }

  /**
   * 文字列化。デバッグ用
   */
  @Override
  public String toString() {
    return kanji + "," + yomi + "," + okuriIni + "," + this.wholeLength();
  }

  /**
   * Gets the kanji string.
   */
  public String getKanji() {
    return kanji;
  }

  /**
   * Gets the yomi string.
   */
  public String getYomi() {
    return yomi;
  }

  /**
   * Gets the okurigana character.
   */
  public char getOkuriIni() {
    return okuriIni;
  }

  /**
   *　漢字部分の長さと送り仮名の長さを和を返す。
   * ただし、漢字部分としては、最初の漢字を除いた残りの部分。
   * 「悪代官」の場合は「代官」で2になる。
   */
  public int wholeLength() {
    return kanjiLength + (okuriIni > 0 ? 1 : 0);
  }

  /**
   * チェック対称の文字列が「この漢字」に一致する場合のよみを取得する。
   * ただし、チェック対称の文字列は「この漢字」の長さよりも短い場合も長い場合もある。
   * また、「この漢字」に送り仮名が設定されている場合には、その送り仮名も含めて一致しなければならない。
   * 
   * @param target　チェック対象文字列
   * @return よみ文字列
   */
  private String doGetYomiFor(String target) {
    
    if (kanjiLength > 0 && !target.startsWith(kanji)) {
      return null;
    }
    
    // 送り仮名が無い場合、yomiをそのまま帰す。
    if (okuriIni == 0) {
      return yomi;
    }

    // 送り仮名がある場合、チェック対称の送り仮名が適当であるか調べ、適当であれば、その文字を加えて返す。
    if (target.length() <= kanjiLength) return null;
    char ch = target.charAt(kanjiLength);
    if (!OkuriganaTable.getInstance().check(ch, okuriIni)) return null;
    return yomi + ch;
  }

  public String getYomiFor(String target) {
    String result = doGetYomiFor(target);
    if (result == null)
      return null;
    //ystem.out.println("" + target + "..." + result);
    return result;
  }

  /**
   * Compares two objects for equality.
   * 
   * @param object
   *          the object to compare with.
   * @return true if the objects are the same; false otherwise.
   */
  public boolean equals(Object object) {
    if (object instanceof KanjiYomi) {
      KanjiYomi kanjiYomi = (KanjiYomi) object;
      return hashCode() == kanjiYomi.hashCode()
          && kanji.equals(kanjiYomi.kanji) && yomi.equals(kanjiYomi.yomi)
          && okuriIni == kanjiYomi.okuriIni;
    }
    return false;
  }

  /**
   * Returns a hash code value for this object.
   */
  public int hashCode() {
    return hashCode;
  }

  /**
   * {@link KanjiYomi}オブジェクトは{@link KanjiYomiList}中で、漢字の長い順に並べられなければ
   * ならないので、そのための比較を行う。
   * ただし、漢字の長さとしては{@link #wholeLength()}が使用されることに注意。
   * {@link #wholeLength()}を参照のこと。
   */
  @Override
  public int compareTo(KanjiYomi that) {
    // 漢字長さを比較する    
    int r = this.wholeLength() - that.wholeLength();
    if (r != 0) return -r;
    
    // 全く同じ漢字。ありえないはずだが。。
    if (this.equals(that)) return 0;
    
    // 異なる漢字で同じ長さ。生成順に並べる
    return this.objectIndex < that.objectIndex ? -1 : 1;
  }

}