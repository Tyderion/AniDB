// File_VorbisCom - Info for VorbisComments tagged files
// Copyright (C) 2007-2007 Jerome Martinez, Zen@MediaArea.net
//
// This library is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this library. If not, see <http://www.gnu.org/licenses/>.
//
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
// Information about Vorbis comments
//
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//---------------------------------------------------------------------------
#ifndef MediaInfo_File_VorbisComH
#define MediaInfo_File_VorbisComH
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
#include "MediaInfo/File__Analyze.h"
//---------------------------------------------------------------------------

namespace MediaInfoLib
{

//***************************************************************************
// Class File_VorbisCom
//***************************************************************************

class File_VorbisCom : public File__Analyze
{
public :
    //In
    stream_t StreamKind;

private :
    //Buffer
    void Header_Parse();
    bool Data_Parse();

    //Elements
    void Comment();

    //Temp
    int32u vendor_length;
    int32u user_comment_list_length;
    int32u user_comment_length;
    Ztring Chapter_Pos;
    Ztring Chapter_Time;
    
protected :
    //Information
    void HowTo (stream_t StreamKind);
};

//***************************************************************************
// Class File_VorbisCom_Helper
//***************************************************************************

class File_VorbisCom_Helper
{
public :
    File_VorbisCom_Helper(File__Base* Base_);
    ~File_VorbisCom_Helper();

protected :
    //Temp
    File_VorbisCom* VorbisCom;

    //From elsewhere
    bool VorbisCom_Read_Buffer_Continue ();
    void VorbisCom_Read_Buffer_Finalize ();

    //Data
    File__Base* Base;
};

} //NameSpace

#endif