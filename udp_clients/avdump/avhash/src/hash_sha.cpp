// Copyright (C) 2006 epoximator
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

#include "hash_wrapper.h"

#include "../src/SHAHashSet.h"
#include "../src/SHA.h"
#include "../src/Encoder.h"

DigestSHA::DigestSHA(){
	type = HASH_SHA1;
	addr = (void*)new CSHA();
}
void DigestSHA::clean(){
	CSHA* sha = (CSHA*)addr;
	delete sha;
}
void DigestSHA::update(char* buf, int len){
	CSHA* sha = (CSHA*)addr;
	sha->Add(buf, len);
}
int DigestSHA::digest(char* sum, int len){
	CSHA* sha = (CSHA*)addr;
	CAICHHash hash;
	sha->Finish(hash);

	string s = Encoder::toBase16(hash.GetRawHash(), hash.GetHashSize());
	strcpy_s((char*)sum, len,s.c_str());
	return (int)s.length();
}
