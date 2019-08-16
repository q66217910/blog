# 这个不是很好，会出现负值
SELECT
	c.created,
	( SELECT count( 1 ) FROM t_contents c3 WHERE c3.created > c.created ) AS crank,
	( SELECT count( 1 ) FROM t_contents c2 WHERE c2.rank > c.rank OR ( c.rank = c2.rank AND c2.created > c.created )) AS crrank,
	c.rank,
	(c.rank - power( 100 - c.rank, 1.0 / 5 ) * ( ( ( UNIX_TIMESTAMP( now( ) ) - c.created ) / 86400 / 100 ) )) AS compute_rank
FROM
	t_contents c
ORDER BY
	compute_rank DESC,
	c.created DESC;


# 这个不行，时间越大，值反而越小了
SELECT
	c.created,
	( SELECT count( 1 ) FROM t_contents c3 WHERE c3.created > c.created ) AS crank,
	( SELECT count( 1 ) FROM t_contents c2 WHERE c2.rank > c.rank OR ( c.rank = c2.rank AND c2.created > c.created )) AS crrank,
	c.rank,
	(c.rank - c.rank * power(0.99, power((UNIX_TIMESTAMP(now()) - c.created) / 86400 / 100, 1.0 / 5)) ) AS compute_rank
FROM
	t_contents c
ORDER BY
	compute_rank DESC,
	c.created DESC;


# 作为排序依据把
SELECT
	c.created,
	( SELECT count( 1 ) FROM t_contents c3 WHERE c3.created > c.created ) AS crank,
	( SELECT count( 1 ) FROM t_contents c2 WHERE c2.rank > c.rank OR ( c.rank = c2.rank AND c2.created > c.created )) AS crrank,
	c.rank,
	(c.rank - c.rank * pow(0.90, 25 / pow((UNIX_TIMESTAMP(now()) - c.created) / 86400 / 108, 1.0 / 5))) AS compute_rank
FROM
	t_contents c
ORDER BY
	compute_rank DESC,
	c.created DESC;
