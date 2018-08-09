SELECT c.contentid, c.contenttype, sp.spacekey, c.creator, c.lastmodifier, c.lastmoddate FROM content c
INNER JOIN spaces sp
ON sp.spaceid = c.spaceid
WHERE c.contenttype = 'PAGE'
      AND c.content_status = 'current'
      AND sp.spacekey IN (SELECT "AFFECTED_SPACE_KEY" FROM "AO_DB634A_AFFECTED_SPACES")
      AND c.lastmoddate < ? --for test only. Should be "<".
      AND c.creator IN
          (
            SELECT DISTINCT um.user_key FROM cwd_user cu
            INNER JOIN user_mapping um
            ON cu.user_name = um.username
            INNER JOIN cwd_membership cm
            ON cu.id = cm.child_user_id
            INNER JOIN cwd_group cg
            ON cg.id = cm.parent_id
            where cu.active = 'T'
                  AND cg.group_name IN (SELECT "AFFECTED_GROUP" FROM "AO_DB634A_AFFECTED_GROUPS")
          )