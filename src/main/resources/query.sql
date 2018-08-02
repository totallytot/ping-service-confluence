select c.contentid, c.contenttype, sp.spacekey, c.creator, c.lastmodifier, c.lastmoddate from confluence.content c
  inner join confluence.spaces sp
  on sp.spaceid = c.spaceid
  where c.contenttype = 'PAGE'
        and c.content_status = 'current'
        and sp.spacekey in ('TEST') --monitored space keys
        and c.lastmoddate <  '2018-07-15 19:35:10.441' /*-- current date-timeframe*/
        and c.creator in
          (
            select distinct um.user_key from confluence.cwd_user cu
            inner join confluence.user_mapping um
            on cu.user_name = um.username
            inner join confluence.cwd_membership cm
            on cu.id = cm.child_user_id
            inner join confluence.cwd_group cg
            on cg.id = cm.parent_id
            where cu.active = 'T'
                  and cg.group_name in ('confluence-users')) --affected groups