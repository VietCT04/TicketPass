"use client";

import { useEffect, useState } from "react";

type EventDateTimeProps = {
  value: string;
};

export function EventDateTime({ value }: EventDateTimeProps) {
  const [formattedValue, setFormattedValue] = useState(formatEventDate(value));

  useEffect(() => {
    setFormattedValue(formatEventDate(value));
  }, [value]);

  return <time dateTime={value}>{formattedValue}</time>;
}

function formatEventDate(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
    timeZoneName: "short"
  }).format(new Date(value));
}
