SELECT DISTINCT C.CONTENTID
FROM CONTENT C
  INNER JOIN SPACES SP
    ON SP.SPACEID = C.SPACEID
WHERE C.CONTENTTYPE = 'PAGE'
      AND C.CONTENT_STATUS = 'current'
      AND SP.SPACEKEY IN (SELECT AFFECTED_SPACE_KEY
                          FROM AO_DB634A_AFFECTED_SPACES)
      AND C.LASTMODDATE < ?
      AND C.CREATOR IN
          (
            SELECT DISTINCT UM.USER_KEY
            FROM cwd_user CU
              INNER JOIN user_mapping UM
                ON CU.USER_NAME = UM.USERNAME
              INNER JOIN cwd_membership CM
                ON CU.ID = CM.CHILD_USER_ID
              INNER JOIN cwd_group CG
                ON CG.ID = CM.PARENT_ID
            WHERE CU.ACTIVE = 'T'
                  AND CG.GROUP_NAME IN (SELECT AFFECTED_GROUP
                                        FROM AO_DB634A_AFFECTED_GROUPS)
          )