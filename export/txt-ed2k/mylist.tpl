<tmpl_loop name=loop_anime><tmpl_loop name=loop_ep><tmpl_loop name=loop_file><tmpl_if expr="data_file_crc ne ''">ed2k://|file|<tmpl_var name=data_anime_name> - <tmpl_var name=data_ep_epno><tmpl_if expr="data_file_group_shortname ne ''"> [<tmpl_var name=data_file_group_shortname>]</tmpl_if>[<tmpl_var name=data_file_crc>].<tmpl_var name=data_file_filetype>|<tmpl_var name=data_file_size_plain>|<tmpl_var name=data_file_ed2k_hash>|/
</tmpl_if></tmpl_loop></tmpl_loop></tmpl_loop>