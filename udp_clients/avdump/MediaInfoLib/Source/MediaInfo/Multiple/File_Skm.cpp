// File_Flv - Info for Flash files
// Copyright (C) 2006-2007 Jerome Martinez, Zen@MediaArea.net
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

//---------------------------------------------------------------------------
// Compilation conditions
#include <MediaInfo/Setup.h>
#ifdef __BORLANDC__
    #pragma hdrstop
#endif
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
#if defined(MEDIAINFO_SKM_YES)
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
#include "MediaInfo/Multiple/File_Skm.h"
#if defined(MEDIAINFO_MPEG4V_YES)
    #include "MediaInfo/Video/File_Mpeg4v.h"
#endif
#include "ZenLib/Utils.h"
//---------------------------------------------------------------------------

namespace MediaInfoLib
{

//***************************************************************************
// Format
//***************************************************************************

//---------------------------------------------------------------------------
void File_Skm::Read_Buffer_Continue()
{
    if (File_Offset==0)
    {
        //Integrity
        if (Buffer_Size<5)
            return;

        //Header
        if (CC5(Buffer)!=CC5("DMSKM"))
        {
            File_Offset=File_Size;
            return;
        }
    }
    else if (Count_Get(Stream_General)==0)
        return; //Not in substreams

    //Filling
    Stream_Prepare(Stream_General);
    Fill("Format", "SKM");

    //Basic parsing
    while(Buffer_Parse());

    FINNISHED();
}

//***************************************************************************
// Buffer
//***************************************************************************

//---------------------------------------------------------------------------
bool File_Skm::Buffer_Parse()
{
    //Next frame
    if (!NextFrame())
        return false;

    //Element size
    if (!Element_Size_Get())
        return false;

    //Parsing
    Element_Parse();

    return false;
}

//---------------------------------------------------------------------------
//
bool File_Skm::Element_Parse()
{
    #if defined(MEDIAINFO_MPEG4V_YES)
        File__Base* MI=new File_Mpeg4v();
        Open_Buffer_Init(MI, Buffer_Offset+Element_Size, File_Offset+Buffer_Offset);
        Open_Buffer_Continue(MI, Buffer+Buffer_Offset, Element_Size);
        if (MI->Count_Get(Stream_Video)>0)
        {
            Open_Buffer_Finalize(MI);
            Merge(*MI);
            File_Offset=File_Size;
        }
        delete MI; //MI=NULL;
    #endif

    Buffer_Offset+=Element_Size;
    return false;
}

//---------------------------------------------------------------------------
bool File_Skm::NextFrame()
{
    //Look for first Sync word
    if (Buffer_Offset_Temp==0) //Buffer_Offset_Temp is not 0 if NextFrame() has already parsed first bytes
        Buffer_Offset_Temp=Buffer_Offset;
    while (Buffer_Offset_Temp+4<=Buffer_Size && !(CC3(Buffer+Buffer_Offset_Temp)==0x000001 && CC1(Buffer+Buffer_Offset_Temp+3)>=0x20)) //0x20 is the only useful value I found in SKM
        Buffer_Offset_Temp++;

    //Not synched case
    if (!Synched && Buffer_Offset_Temp+4>Buffer_Size)
    {
        if (Buffer_Offset_Temp+3==Buffer_Size)
        {
            if (CC3(Buffer+Buffer_Offset_Temp)!=0x000001)
            {
                Buffer_Offset_Temp++;
                if (CC2(Buffer+Buffer_Offset_Temp)!=0x0000)
                {
                    Buffer_Offset_Temp++;
                    if (CC1(Buffer+Buffer_Offset_Temp)!=0x00)
                        Buffer_Offset_Temp++;
                }
            }
        }
        Buffer_Offset=Buffer_Offset_Temp;
        Buffer_Offset_Temp=0;
        return false;
    }

    //Must wait more data?
    if (Buffer_Offset_Temp+4>Buffer_Size)
        return false;

    //Error in stream?
    if (Buffer_Offset_Temp-Buffer_Offset>0)
    {
        if (Synched)
            TRUSTED_ISNOT("Sync error", 0, Buffer_Offset_Temp-Buffer_Offset);
        else
        {
            ELEMENT(0, "Synchronization", Buffer_Offset_Temp-Buffer_Offset);
            FLUSH();
        }
    }

    //OK, we continue
    Buffer_Offset=Buffer_Offset_Temp;
    Buffer_Offset_Temp=0;
    Synched=true;
    return true;
}

//---------------------------------------------------------------------------
bool File_Skm::Element_Size_Get()
{
    size_t Element_Next=Buffer_Offset+4;
    while (Element_Next+4<=Buffer_Size && !(CC3(Buffer+Element_Next)==0x000001))
        Element_Next++;
    if (Element_Next+4>Buffer_Size) //This is the last frame (truncated)
        return false;

    Element_Size=Element_Next-Buffer_Offset;
    return true;
}

//***************************************************************************
// Information
//***************************************************************************

//---------------------------------------------------------------------------
void File_Skm::HowTo(stream_t StreamKind)
{
    switch (StreamKind)
    {
        case (Stream_General) :
            Fill_HowTo("Format", "R");
            break;
        case (Stream_Video) :
            break;
        case (Stream_Audio) :
            break;
        case (Stream_Text) :
            break;
        case (Stream_Chapters) :
            break;
        case (Stream_Image) :
            break;
        case (Stream_Menu) :
            break;
        case (Stream_Max) :
            break;
    }
}

} //NameSpace

#endif //MEDIAINFO_SKM_YES
