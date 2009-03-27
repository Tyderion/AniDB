﻿/******************************************************************************
 *
 * Jacksum version 1.5.0 - checksum utility in Java
 * Copyright (C) 2001-2004 Dipl.-Inf. (FH) Johann Nepomuk Loefflmann,
 * All Rights Reserved, http://www.jonelo.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Edonkey is an implementation of the AbstractChecksum in order to calculate a
 * message digest in the form of edonkey/emule hash. It uses the MD4 algorithm
 * as an auxiliary algorithm, from the GNU crypto project,
 * http://www.gnu.org/software/classpathx/crypto
 *
 *****************************************************************************
 * Modified and ported to C# by DvdKhl
 *****************************************************************************/

using System;
using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography;
using System.Diagnostics;

namespace AVDump2Lib.Hashes {
    /// <summary>Broken: Blocks need to be Blocksize*n=9500*1024 (n as int)</summary>
    public class Ed2k : HashAlgorithm {
        private static int BLOCKSIZE;
        private byte[] md4HashBlock;
        private long length;
        private HashAlgorithm md4;
        private HashAlgorithm md4final;

        public Ed2k() {
            md4HashBlock = new byte[16];
            md4 = new Md4();
            md4final = new Md4();
        }

        protected override void HashCore(byte[] array, int ibStart, int cbSize) {
            int toWrite = cbSize - ibStart;
            int space = BLOCKSIZE - (int)(length % BLOCKSIZE);

            if(space > toWrite){
                md4.TransformBlock(array, ibStart, cbSize, array, 0);
                length += cbSize;
            } else if(space == toWrite) {
                md4.TransformFinalBlock(array, ibStart, cbSize);
                md4final.TransformBlock(md4.Hash, 0, 16, md4.Hash, 0);
                md4.Initialize();
                length += cbSize;

            } else if(space < toWrite){
                //TODO: BROKEN
                md4.TransformFinalBlock(array, ibStart, space);
                md4final.TransformBlock(md4.Hash, 0, 16, md4.Hash, 0);
                md4.Initialize();
                length += space;

                md4.TransformBlock(array, ibStart + space, toWrite - space, array, 0);
                length += toWrite - space;
            }

        }

        protected override byte[] HashFinal() {
            md4.TransformFinalBlock(new byte[0], 0, 0);

            if(length < BLOCKSIZE){
                md4final = md4;
            }else{
                md4final.TransformFinalBlock(md4.Hash, 0, 16);
            }

            return md4final.Hash;
        }

        public override void Initialize() {
            Array.Clear(md4HashBlock, 0, md4HashBlock.Length);
            length = 0;
            BLOCKSIZE = 9728000;
            md4.Initialize();
            md4final.Initialize();
        }
    }

    public class Ed2kAlt : HashAlgorithm {
        private static int BLOCKSIZE;
        private List<byte[]> md4HashBlocks;
        private long length;
        private HashAlgorithm md4;

        public Ed2kAlt() {
            md4HashBlocks = new List<byte[]>();
            md4 = new Md4();
        }

        protected override void HashCore(byte[] array, int ibStart, int cbSize) {
            int toWrite = cbSize - ibStart;
            int space = BLOCKSIZE - (int)(length % BLOCKSIZE);

            if(space > toWrite){
                md4.TransformBlock(array, ibStart, cbSize, array, 0);
                length += cbSize;
            } else if(space == toWrite) {
                md4.TransformFinalBlock(array, ibStart, cbSize);
                md4HashBlocks.Add(md4.Hash); 
                md4.Initialize();
                length += cbSize;

            } else if(space < toWrite){
                //TODO: BROKEN
                md4.TransformFinalBlock(array, ibStart, space);
                md4HashBlocks.Add(md4.Hash); 
                md4.Initialize();
                length += space;

                md4.TransformBlock(array, ibStart + space, toWrite - space, array, 0);
                length += toWrite - space;
            }

        }

        protected override byte[] HashFinal() {
            md4.TransformFinalBlock(new byte[0], 0, 0);

            if(length >= BLOCKSIZE){
                md4HashBlocks.Add(md4.Hash);
                md4.Initialize();
                for(int i = 0;i < md4HashBlocks.Count;i++) {
                    md4.TransformBlock(md4HashBlocks[i], 0, 16, md4HashBlocks[i], 0);
                }
                md4.TransformFinalBlock(new byte[0], 0, 0);
            }

            return md4.Hash;
        }

        public override void Initialize() {
            md4HashBlocks.Clear();
            length = 0;
            BLOCKSIZE = 9728000;
            md4.Initialize();
        }
    }
}
