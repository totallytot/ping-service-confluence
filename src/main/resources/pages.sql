select c.contentid, c.contenttype, sp.spacekey, c.creator, c.lastmodifier, c.lastmoddate from content c
inner join spaces sp
on sp.spaceid = c.spaceid
where c.contenttype = 'PAGE'
      and c.content_status = 'current'
      and sp.spacekey in (?) --monitored space keys
      and c.lastmoddate > ? -- current date-timeframe
      and c.creator in
          (
            select distinct um.user_key from cwd_user cu
            inner join user_mapping um
            on cu.user_name = um.username
            inner join cwd_membership cm
            on cu.id = cm.child_user_id
            inner join cwd_group cg
            on cg.id = cm.parent_id
            where cu.active = 'T'
                  and cg.group_name in (?) --affected groups
          )