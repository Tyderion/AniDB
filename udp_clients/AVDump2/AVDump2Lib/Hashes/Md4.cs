﻿// MD4Managed.cs - Message Digest 4 Managed Implementation
//
// Author:
//	Sebastien Pouliot (sebastien@ximian.com)
//
// (C) 2003 Motus Technologies Inc. (http://www.motus.com)
// Copyright (C) 2004-2005 Novell, Inc (http://www.novell.com)
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
// 
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

using System;
using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography;
using System.Diagnostics;
using System.Threading;

namespace AVDump2Lib.Hashes {
    public class Md4 : HashAlgorithm {
        public static int HASHLENGTH = 16;
        public static int BLOCKLENGTH = 64;
        private static uint A0, B0, C0, D0;
        private static string HASH0 = "31D6CFE0D16AE931B73C59D7E0C089C0";

        private uint A, B, C, D;
        private long length;
        private byte[] buffer;

        static string bla = "astr";

        public Md4() {
            A0 = 0x67452301U;
            B0 = 0xEFCDAB89U;
            C0 = 0x98BADCFEU;
            D0 = 0x10325476U;
            buffer = new byte[BLOCKLENGTH];
        }

        protected override void HashCore(byte[] array, int ibStart, int cbSize) {
            int n = (int)(length % BLOCKLENGTH);
            int partLen = BLOCKLENGTH - n;
            int i = 0;
            length += cbSize;


            if(cbSize >= partLen) {
                System.Buffer.BlockCopy(array, ibStart, buffer, n, partLen);
                TransformMd4Block(buffer, 0);
                i = partLen;
                while(i + BLOCKLENGTH - 1 < cbSize) {
                    TransformMd4Block(array, ibStart + i);
                    i += BLOCKLENGTH;
                }
                n = 0;
            }
            if(i < cbSize) System.Buffer.BlockCopy(array, ibStart + i, buffer, n, cbSize - i);
        }

        protected override byte[] HashFinal() {
            byte[] tail = PadBuffer();
            HashCore(tail, 0, tail.Length);

            return new byte[] {
                (byte)(A), (byte)(A >> 8), (byte)(A >> 16), (byte)(A >> 24), 
                (byte)(B), (byte)(B >> 8), (byte)(B >> 16), (byte)(B >> 24), 
                (byte)(C), (byte)(C >> 8), (byte)(C >> 16), (byte)(C >> 24), 
                (byte)(D), (byte)(D >> 8), (byte)(D >> 16), (byte)(D >> 24) 
            };
        }

        public override void Initialize() {
            A = A0;
            B = B0;
            C = C0;
            D = D0;
            length = 0;
        }

        protected byte[] PadBuffer() {
            int padding;
            int n = (int)(length % BLOCKLENGTH);
            if(n < 56) padding = 56 - n; else padding = 120 - n;
            long bits = length << 3;

            byte[] pad = new byte[padding + 8];
            pad[0] = 0x80;
            pad[padding] = (byte)(bits & 0xFF);
            pad[padding + 1] = (byte)(bits >> 8 & 0xFF);
            pad[padding + 2] = (byte)(bits >> 16 & 0xFF);
            pad[padding + 3] = (byte)(bits >> 24 & 0xFF);
            pad[padding + 4] = (byte)(bits >> 32 & 0xFF);
            pad[padding + 5] = (byte)(bits >> 40 & 0xFF);
            pad[padding + 6] = (byte)(bits >> 48 & 0xFF);
            pad[padding + 7] = (byte)(bits >> 56 & 0xFF);
            return pad;
        }

        protected void TransformMd4Block(byte[] bytes, int i) {
            uint x0, x1, x2, x3, x4, x5, x6, x7, x8, x9, xA, xB, xC, xD, xE, xF;
            x0 = (bytes[i] & 0xFFU) | (bytes[i + 1] & 0xFFU) << 8 | (bytes[i + 2] & 0xFFU) << 16 | (uint)(bytes[i + 3]) << 24;
            x1 = (bytes[i + 4] & 0xFFU) | (bytes[i + 5] & 0xFFU) << 8 | (bytes[i + 6] & 0xFFU) << 16 | (uint)(bytes[i + 7]) << 24;
            x2 = (bytes[i + 8] & 0xFFU) | (bytes[i + 9] & 0xFFU) << 8 | (bytes[i + 10] & 0xFFU) << 16 | (uint)(bytes[i + 11]) << 24;
            x3 = (bytes[i + 12] & 0xFFU) | (bytes[i + 13] & 0xFFU) << 8 | (bytes[i + 14] & 0xFFU) << 16 | (uint)(bytes[i + 15]) << 24;
            x4 = (bytes[i + 16] & 0xFFU) | (bytes[i + 17] & 0xFFU) << 8 | (bytes[i + 18] & 0xFFU) << 16 | (uint)(bytes[i + 19]) << 24;
            x5 = (bytes[i + 20] & 0xFFU) | (bytes[i + 21] & 0xFFU) << 8 | (bytes[i + 22] & 0xFFU) << 16 | (uint)(bytes[i + 23]) << 24;
            x6 = (bytes[i + 24] & 0xFFU) | (bytes[i + 25] & 0xFFU) << 8 | (bytes[i + 26] & 0xFFU) << 16 | (uint)(bytes[i + 27]) << 24;
            x7 = (bytes[i + 28] & 0xFFU) | (bytes[i + 29] & 0xFFU) << 8 | (bytes[i + 30] & 0xFFU) << 16 | (uint)(bytes[i + 31]) << 24;
            x8 = (bytes[i + 32] & 0xFFU) | (bytes[i + 33] & 0xFFU) << 8 | (bytes[i + 34] & 0xFFU) << 16 | (uint)(bytes[i + 35]) << 24;
            x9 = (bytes[i + 36] & 0xFFU) | (bytes[i + 37] & 0xFFU) << 8 | (bytes[i + 38] & 0xFFU) << 16 | (uint)(bytes[i + 39]) << 24;
            xA = (bytes[i + 40] & 0xFFU) | (bytes[i + 41] & 0xFFU) << 8 | (bytes[i + 42] & 0xFFU) << 16 | (uint)(bytes[i + 43]) << 24;
            xB = (bytes[i + 44] & 0xFFU) | (bytes[i + 45] & 0xFFU) << 8 | (bytes[i + 46] & 0xFFU) << 16 | (uint)(bytes[i + 47]) << 24;
            xC = (bytes[i + 48] & 0xFFU) | (bytes[i + 49] & 0xFFU) << 8 | (bytes[i + 50] & 0xFFU) << 16 | (uint)(bytes[i + 51]) << 24;
            xD = (bytes[i + 52] & 0xFFU) | (bytes[i + 53] & 0xFFU) << 8 | (bytes[i + 54] & 0xFFU) << 16 | (uint)(bytes[i + 55]) << 24;
            xE = (bytes[i + 56] & 0xFFU) | (bytes[i + 57] & 0xFFU) << 8 | (bytes[i + 58] & 0xFFU) << 16 | (uint)(bytes[i + 59]) << 24;
            xF = (bytes[i + 60] & 0xFFU) | (bytes[i + 61] & 0xFFU) << 8 | (bytes[i + 62] & 0xFFU) << 16 | (uint)(bytes[i + 63]) << 24;

            uint aa, bb, cc, dd;

            aa = A;
            bb = B;
            cc = C;
            dd = D;

            aa += ((bb & cc) | ((~bb) & dd)) + x0;
            aa = aa << 3 | aa >> -3;
            dd += ((aa & bb) | ((~aa) & cc)) + x1;
            dd = dd << 7 | dd >> -7;
            cc += ((dd & aa) | ((~dd) & bb)) + x2;
            cc = cc << 11 | cc >> -11;
            bb += ((cc & dd) | ((~cc) & aa)) + x3;
            bb = bb << 19 | bb >> -19;
            aa += ((bb & cc) | ((~bb) & dd)) + x4;
            aa = aa << 3 | aa >> -3;
            dd += ((aa & bb) | ((~aa) & cc)) + x5;
            dd = dd << 7 | dd >> -7;
            cc += ((dd & aa) | ((~dd) & bb)) + x6;
            cc = cc << 11 | cc >> -11;
            bb += ((cc & dd) | ((~cc) & aa)) + x7;
            bb = bb << 19 | bb >> -19;
            aa += ((bb & cc) | ((~bb) & dd)) + x8;
            aa = aa << 3 | aa >> -3;
            dd += ((aa & bb) | ((~aa) & cc)) + x9;
            dd = dd << 7 | dd >> -7;
            cc += ((dd & aa) | ((~dd) & bb)) + xA;
            cc = cc << 11 | cc >> -11;
            bb += ((cc & dd) | ((~cc) & aa)) + xB;
            bb = bb << 19 | bb >> -19;
            aa += ((bb & cc) | ((~bb) & dd)) + xC;
            aa = aa << 3 | aa >> -3;
            dd += ((aa & bb) | ((~aa) & cc)) + xD;
            dd = dd << 7 | dd >> -7;
            cc += ((dd & aa) | ((~dd) & bb)) + xE;
            cc = cc << 11 | cc >> -11;
            bb += ((cc & dd) | ((~cc) & aa)) + xF;
            bb = bb << 19 | bb >> -19;

            aa += ((bb & (cc | dd)) | (cc & dd)) + x0 + 0x5A827999U;
            aa = aa << 3 | aa >> -3;
            dd += ((aa & (bb | cc)) | (bb & cc)) + x4 + 0x5A827999U;
            dd = dd << 5 | dd >> -5;
            cc += ((dd & (aa | bb)) | (aa & bb)) + x8 + 0x5A827999U;
            cc = cc << 9 | cc >> -9;
            bb += ((cc & (dd | aa)) | (dd & aa)) + xC + 0x5A827999U;
            bb = bb << 13 | bb >> -13;
            aa += ((bb & (cc | dd)) | (cc & dd)) + x1 + 0x5A827999U;
            aa = aa << 3 | aa >> -3;
            dd += ((aa & (bb | cc)) | (bb & cc)) + x5 + 0x5A827999U;
            dd = dd << 5 | dd >> -5;
            cc += ((dd & (aa | bb)) | (aa & bb)) + x9 + 0x5A827999U;
            cc = cc << 9 | cc >> -9;
            bb += ((cc & (dd | aa)) | (dd & aa)) + xD + 0x5A827999U;
            bb = bb << 13 | bb >> -13;
            aa += ((bb & (cc | dd)) | (cc & dd)) + x2 + 0x5A827999U;
            aa = aa << 3 | aa >> -3;
            dd += ((aa & (bb | cc)) | (bb & cc)) + x6 + 0x5A827999U;
            dd = dd << 5 | dd >> -5;
            cc += ((dd & (aa | bb)) | (aa & bb)) + xA + 0x5A827999U;
            cc = cc << 9 | cc >> -9;
            bb += ((cc & (dd | aa)) | (dd & aa)) + xE + 0x5A827999U;
            bb = bb << 13 | bb >> -13;
            aa += ((bb & (cc | dd)) | (cc & dd)) + x3 + 0x5A827999U;
            aa = aa << 3 | aa >> -3;
            dd += ((aa & (bb | cc)) | (bb & cc)) + x7 + 0x5A827999U;
            dd = dd << 5 | dd >> -5;
            cc += ((dd & (aa | bb)) | (aa & bb)) + xB + 0x5A827999U;
            cc = cc << 9 | cc >> -9;
            bb += ((cc & (dd | aa)) | (dd & aa)) + xF + 0x5A827999U;
            bb = bb << 13 | bb >> -13;

            aa += (bb ^ cc ^ dd) + x0 + 0x6ED9EBA1U;
            aa = aa << 3 | aa >> -3;
            dd += (aa ^ bb ^ cc) + x8 + 0x6ED9EBA1U;
            dd = dd << 9 | dd >> -9;
            cc += (dd ^ aa ^ bb) + x4 + 0x6ED9EBA1U;
            cc = cc << 11 | cc >> -11;
            bb += (cc ^ dd ^ aa) + xC + 0x6ED9EBA1U;
            bb = bb << 15 | bb >> -15;
            aa += (bb ^ cc ^ dd) + x2 + 0x6ED9EBA1U;
            aa = aa << 3 | aa >> -3;
            dd += (aa ^ bb ^ cc) + xA + 0x6ED9EBA1U;
            dd = dd << 9 | dd >> -9;
            cc += (dd ^ aa ^ bb) + x6 + 0x6ED9EBA1U;
            cc = cc << 11 | cc >> -11;
            bb += (cc ^ dd ^ aa) + xE + 0x6ED9EBA1U;
            bb = bb << 15 | bb >> -15;
            aa += (bb ^ cc ^ dd) + x1 + 0x6ED9EBA1U;
            aa = aa << 3 | aa >> -3;
            dd += (aa ^ bb ^ cc) + x9 + 0x6ED9EBA1U;
            dd = dd << 9 | dd >> -9;
            cc += (dd ^ aa ^ bb) + x5 + 0x6ED9EBA1U;
            cc = cc << 11 | cc >> -11;
            bb += (cc ^ dd ^ aa) + xD + 0x6ED9EBA1U;
            bb = bb << 15 | bb >> -15;
            aa += (bb ^ cc ^ dd) + x3 + 0x6ED9EBA1U;
            aa = aa << 3 | aa >> -3;
            dd += (aa ^ bb ^ cc) + xB + 0x6ED9EBA1U;
            dd = dd << 9 | dd >> -9;
            cc += (dd ^ aa ^ bb) + x7 + 0x6ED9EBA1U;
            cc = cc << 11 | cc >> -11;
            bb += (cc ^ dd ^ aa) + xF + 0x6ED9EBA1U;
            bb = bb << 15 | bb >> -15;

            A += aa;
            B += bb;
            C += cc;
            D += dd;
        }
    }
}
